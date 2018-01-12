package com.zhy.stickynavlayout.view;

import com.google.gson.annotations.SerializedName;

/**
 * 轮播图数据项
 */
public class BannerItem  {
    @SerializedName("img")
    private String image;

    public BannerItem(String image){
        this.image = image;
    }
    public String getImage() {
        return image;
    }
}
