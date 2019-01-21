package com.taskapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.taskapp.R;
import com.taskapp.adapter.NewsListAdapter;
import com.taskapp.application.TaskApp;
import com.taskapp.listener.AdapterPositionListener;
import com.taskapp.listener.EndlessRecyclerViewScrollListener;
import com.taskapp.model.NewsModel;
import com.taskapp.server_task.AppHelper;
import com.taskapp.server_task.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<NewsModel> newsList;
    private NewsListAdapter newsListAdapter;
    private int offset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv_news_list = findViewById(R.id.rv_news_list);
        SearchView sv_search = findViewById(R.id.sv_search);
        sv_search.setVisibility(View.VISIBLE);
        newsList = new ArrayList<>();
        getNewsList(0);

        newsListAdapter = new NewsListAdapter(newsList, this, new AdapterPositionListener() {
            @Override
            public void getPosition(int position) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("Position", position);
                startActivity(intent);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv_news_list.setLayoutManager(manager);
        rv_news_list.setAdapter(newsListAdapter);

        // Endless Recycler Scroll Listener Pagination
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Get News List Api
                getNewsList(page);
            }
        };

        // Adds the scroll listener to RecyclerView
        rv_news_list.addOnScrollListener(scrollListener);

        // Search View
        sv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                newsListAdapter.getFilter().filter(newText.trim());
                return true;
            }
        });
    }

    // Get News List Api
    private void getNewsList(final int page) {
        if (AppHelper.isConnectingToInternet(this)) {

            WebService api = new WebService(this, TaskApp.TAG, new WebService.WebResponseListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);

                        JSONObject result = js.getJSONObject("result");
                        newsList.clear();
                        JSONArray jsonArray = result.getJSONArray("data");
                        NewsModel model;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            model = new NewsModel();
                            JSONObject object = jsonArray.getJSONObject(i);
                            model.id = object.getInt("id");
                            model.title = object.getString("title");
                            model.description = object.getString("description");
                            model.city = object.getString("city");
                            model.status = object.getString("status");
                            model.view_count = object.getString("view_count");
                            model.first_name = object.getString("first_name");
                            model.last_name = object.getString("last_name");
                            model.category_title = object.getString("category_title");
                            model.created_at = object.getString("created_at");

                            JSONArray array = object.getJSONArray("images");
                            for (int j = 0; j < array.length(); j++) {
                                JSONObject json = array.getJSONObject(0);
                                model.images_url = json.getString("title");
                            }
                            newsList.add(model);
                        }

                        offset = page + 1;
                        newsListAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {

                }

            });
            api.callApi("api/contents?page=" + offset, Request.Method.GET, null, true);
        } else {
            Toast.makeText(this, R.string.alert_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
