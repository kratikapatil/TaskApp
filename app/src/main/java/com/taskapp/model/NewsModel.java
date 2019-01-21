package com.taskapp.model;

import java.util.List;

/**
 * Created by krati on 16/01/2019.
 */

public class NewsModel {
    public int id;
    public String title;
    public String description;
    public String city;
    public String created_at;
    public String status;
    public String view_count;
    public String first_name;
    public String last_name;
    public String category_title;
    public String images_url;
  //  public List<ImagesBean> images;

    public static class ImagesBean {
        /**
         * id : 470
         * title : 4469926241547808334.jpg
         * content_id : 301
         * created_at : 2019-01-18 16:15:36
         */

        public int id;
        public String title;
        public String content_id;
        public String created_at;
    }
}
