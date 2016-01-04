package com.johnpetitto.rxjavaandroidexample;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResult {
  @SerializedName("total_count") private int totalCount;
  private List<SearchItem> items;

  public int getTotalCount() {
    return totalCount;
  }

  public List<SearchItem> getItems() {
    return items;
  }

  @Override public String toString() {
    return new Gson().toJson(this);
  }
}
