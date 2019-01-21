package com.taskapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.taskapp.R;
import com.taskapp.listener.AdapterPositionListener;
import com.taskapp.model.NewsModel;
import com.taskapp.server_task.API;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ViewHolder> implements Filterable  {
    private Context mContext;
    private AdapterPositionListener listener;
    private Date date;
    private SimpleDateFormat format;
    private ArrayList<NewsModel> newsList, mNewsList;
    private ValueFilter valueFilter;

    public NewsListAdapter(ArrayList<NewsModel> newsList, Context mContext, AdapterPositionListener listener) {
        this.newsList = newsList;
        this.mNewsList = newsList;
        this.mContext = mContext;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_news_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsModel model = newsList.get(position);

        Glide.with(mContext).load(API.IMAGE_BASE_URL + model.images_url).into(holder.iv_news);
        holder.tv_title.setText("Title: " + Html.fromHtml(model.title));

        if (model.created_at != null) {
            try {
                date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(model.created_at);
                format = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tv_date.setText(format.format(date).toUpperCase());
        }
    }


    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iv_news;
        TextView tv_title, tv_date;
        CardView cv_news_list;

        ViewHolder(View itemView) {
            super(itemView);

            iv_news = itemView.findViewById(R.id.iv_news);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_date = itemView.findViewById(R.id.tv_date);
            cv_news_list = itemView.findViewById(R.id.cv_news_list);

            cv_news_list.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.cv_news_list:
                    listener.getPosition(getAdapterPosition());
                    break;
            }
        }
    }
    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }
    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence ch) {
            FilterResults results = new FilterResults();

            if (ch != null && ch.length() > 0) {
                ArrayList<NewsModel> filterList = new ArrayList<>();
                for (int i = 0; i < mNewsList.size(); i++) {
                    if ((mNewsList.get(i).title.toUpperCase()).contains(ch.toString().toUpperCase()) || (mNewsList.get(i).description.toUpperCase()).contains(ch.toString().toUpperCase())) {
                        filterList.add(mNewsList.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mNewsList.size();
                results.values = mNewsList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence ch,
                                      FilterResults results) {
            newsList = (ArrayList<NewsModel>) results.values;
            notifyDataSetChanged();
        }

    }

}
