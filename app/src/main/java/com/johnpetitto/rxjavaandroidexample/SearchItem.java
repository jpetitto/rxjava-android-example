package com.johnpetitto.rxjavaandroidexample;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SearchItem {
  private String login;
  @SerializedName("avatar_url") private String avatarUrl;

  public String getLogin() {
    return login;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  @Override public String toString() {
    return new Gson().toJson(this);
  }
}
