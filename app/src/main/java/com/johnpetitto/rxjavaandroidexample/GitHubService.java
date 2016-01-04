package com.johnpetitto.rxjavaandroidexample;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface GitHubService {
  @GET("/search/users?")
  Observable<SearchResult> searchUsers(@Query("q") String query);
}
