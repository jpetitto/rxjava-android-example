package com.johnpetitto.rxjavaandroidexample;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SearchItem {
  private String login;
  private int id;
  @SerializedName("avatar_url") private String avatarUrl;
  @SerializedName("html_url") private String url;

  @Override public String toString() {
    return new Gson().toJson(this);
  }
}
