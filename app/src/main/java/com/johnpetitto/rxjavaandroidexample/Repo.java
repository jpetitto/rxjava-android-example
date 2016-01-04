package com.johnpetitto.rxjavaandroidexample;

import com.google.gson.Gson;

public class Repo {
  private String name;

  @Override public String toString() {
    return new Gson().toJson(this);
  }
}
