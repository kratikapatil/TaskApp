package com.taskapp.server_task;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.taskapp.application.TaskApp;
import com.taskapp.volley_request.VolleyMultipartRequest;
import com.taskapp.volley_request.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WebService {

    private Context mContext;
    private String TAG;
    private WebResponseListner mListener;

    public WebService(Context context, String TAG, WebResponseListner listener) {
        super();
        mListener = listener;
        this.mContext = context;
        this.TAG = TAG;
    }


    public void callMultiPartApi(final String url, final Map<String, String> params) {
        callMultiPartApi(url, params, null);
    }

    // for image
    private void callMultiPartApi(final String url, final Map<String, String> params, final Map<String, Bitmap> bitmapList) {
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                API.BASE_URL + url, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                mListener.onResponse(resultResponse, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getNetworkMessage(error);
                Log.i("Error", errorMessage);
                error.printStackTrace();
                handleError(error);
                mListener.ErrorListener(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                for (Map.Entry<String, Bitmap> entry : bitmapList.entrySet()) {
                    String key = entry.getKey();
                    Bitmap bitmap = entry.getValue();
                    params.put(key, new DataPart(key.concat(".jpg"), AppHelper.getFileDataFromDrawable(bitmap), "image/png"));
                }

                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
    }

    private String getNetworkMessage(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = null;
                if (isJSONValid(result)) {
                    response = new JSONObject(result);
                }

                String status = "";
                String message = "";

                if (ServerResponseCode.getmessageCode(networkResponse.statusCode).equals("Ok")) {
                    if (response.has("status")) status = response.getString("status");
                    if (response.has("message")) message = response.getString("message");
                    Log.e("Error Status", "" + status);
                    Log.e("Error Message", message);
                } else {
                    errorMessage = ServerResponseCode.getmessageCode(networkResponse.statusCode);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Error", errorMessage);
        return errorMessage;
    }

    private static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
            // If JsonObject is ok then check for JSONArray
            try {
                new JSONArray(test);
                return true;
            } catch (JSONException ex1) {
                return false;
            }
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
                return true;
            } catch (JSONException ex1) {
                return false;
            }
        }
    }


    public void callApi(final String url, int Method, final Map<String, String> params, final boolean isSelfErrorHandle) {
        StringRequest stringRequest = new StringRequest(Method, API.BASE_URL + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        mListener.onResponse(response, url);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isSelfErrorHandle) {
                            handleError(error);
                        }
                        //handleError(error);

                        mListener.ErrorListener(error);

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params == null)
                    return super.getParams();
                else return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return new HashMap<>();
            }
        };
        TaskApp.getInstance().addToRequestQueue(stringRequest, TAG);
    }


    public void callApiWithHeader(final String url, int Method, final Map<String, String> params, final boolean isSelfErrorHandle) {
        StringRequest stringRequest = new StringRequest(Method, API.BASE_URL + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        mListener.onResponse(response, url);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isSelfErrorHandle) {
                            handleError(error);
                        }
                        //handleError(error);

                        mListener.ErrorListener(error);

                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params == null)
                    return super.getParams();
                else return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjdkZTI1NjVlZjdlZGY0ZTIzYTczMTEyMzc0NTAyMmEzZDA5YzRkZWMwZmFjNmI3YjE3MTMxNGE1NGFkOWJjZDU3ZjI4NDc5OTA1NGFiNGMwIn0.eyJhdWQiOiIxIiwianRpIjoiN2RlMjU2NWVmN2VkZjRlMjNhNzMxMTIzNzQ1MDIyYTNkMDljNGRlYzBmYWM2YjdiMTcxMzE0YTU0YWQ5YmNkNTdmMjg0Nzk5MDU0YWI0YzAiLCJpYXQiOjE1NDc0ODk1OTYsIm5iZiI6MTU0NzQ4OTU5NiwiZXhwIjoxNTc5MDI1NTk2LCJzdWIiOiIxNyIsInNjb3BlcyI6W119.P1Vg7GAIsDMrwZ50yuNwwS89Lh3bsWbzyeWt5Z_DmhDHNndKuA6Bxdx9ECUbj8rOSDTiCFKFWEAJ1eriDlnzvXOZkR_HuOpTVT3VTmONGe5NvyjKiArW2bOhWYhbdWAkIf4Eigj27ANIdHMDglw1jSAaOE8cPtoTL-Nm2SuM0KV6HCzrXvT673njMbmgtN7siYs0Hknh35p_vSaADZdobjiSPKeIVrf1oIIHUfxJ7iBVAbHerzENhnJAl5o2g7uhWPUwqGhyz6BZGwvrsROt8yx0dgMgYgodxexoK-oHFCdFHBnjw0uWQNirs_bk65bRx-A9G8C_aQ-E4i_Lp81RUuSoTMkE2BCrCDQGD7gdNkZC6Th-R5fzhbLvSovt5a5lwnC_fZQJS3bldSb6xUG9EW97ZsLit4mbkAytKtax9zRjk5zJS6NDXbiRepoukMAGw1ysM3C3qG_Q63bQvehnTtp7q_eibgnRrOg-znpCba2nR9pFwFBgtAGDDD2iXF_Mxl36PnqfA56MuxEdlk559ZzE5QPXppQgE9oMxMq8Nor6ysCBxPNy54gBulMkmlbbtgvBNUluc_pJAy_Y9mEAjrFUAZ70uSZtAQwZgjv4YRbVfsoAtZW_hC8RmvpxH4m9bpdE3L0bV8wKkxDDldQGpNjpKJSHsqL_quZNx498bf0");
                return header;
            }
        };
        TaskApp.getInstance().addToRequestQueue(stringRequest, TAG);
    }

    public interface WebResponseListner {
        void onResponse(String responce, String url);

        void ErrorListener(VolleyError error);
    }

    public void setListner(WebResponseListner listner) {
        mListener = listner;
    }


    private void handleError(VolleyError error) {
        handleError(mContext, error);
    }


    private void handleError(Context context, VolleyError error) {

    }

}
