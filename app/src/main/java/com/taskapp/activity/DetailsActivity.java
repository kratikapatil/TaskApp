package com.taskapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.taskapp.R;
import com.taskapp.application.TaskApp;
import com.taskapp.model.NewsModel;
import com.taskapp.server_task.API;
import com.taskapp.server_task.AppHelper;
import com.taskapp.server_task.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by krati on 17/01/2019.
 */

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<NewsModel> newsList;
    private Date date;
    private SimpleDateFormat format;
    private ImageView iv_news;
    private TextView tv_title, tv_description, tv_date;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        int position = getIntent().getIntExtra("Position", 0);

        newsList = new ArrayList<>();
        ImageView iv_back = findViewById(R.id.iv_back);
        ImageView iv_share = findViewById(R.id.iv_share);

        iv_news = findViewById(R.id.iv_news);
        tv_title = findViewById(R.id.tv_title);
        tv_date = findViewById(R.id.tv_date);
        tv_description = findViewById(R.id.tv_description);

        iv_back.setVisibility(View.VISIBLE);
        iv_share.setVisibility(View.VISIBLE);

        iv_back.setOnClickListener(this);
        iv_share.setOnClickListener(this);

        getNewsList(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;

            case R.id.iv_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
        }
    }

    // Get News List Api
    private void getNewsList(final int position) {
        if (AppHelper.isConnectingToInternet(this)) {

            WebService api = new WebService(this, TaskApp.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        Log.e("News List", response);

                        JSONObject result = js.getJSONObject("result");
                        newsList.clear();
                        JSONArray jsonArray = result.getJSONArray("data");

                        NewsModel model = new NewsModel();
                        JSONObject object = jsonArray.getJSONObject(position);
                        model.id = object.getInt("id");
                        model.title = object.getString("title");
                        model.description = object.getString("description");
                        model.created_at = object.getString("created_at");

                        JSONArray array = object.getJSONArray("images");
                        for (int j= 0; j < array.length(); j++) {
                            JSONObject json = array.getJSONObject(0);
                            model.images_url = json.getString("title");
                        }

                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(model.created_at);
                            format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Glide.with(DetailsActivity.this).load(API.IMAGE_BASE_URL + model.images_url).into(iv_news);
                        String title = getString(R.string.title) + Html.fromHtml(model.title);
                        String news_date = getString(R.string.date) + format.format(date).toUpperCase();
                        String description = getString(R.string.description) + Html.fromHtml(model.description);

                        tv_title.setText(title);
                        tv_date.setText(news_date);
                        tv_description.setText(description);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("api/contents" , Request.Method.GET, null, true);
        } else {
            Toast.makeText(this,R.string.alert_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
