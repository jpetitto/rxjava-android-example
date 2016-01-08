package com.johnpetitto.rxjavaandroidexample;

import android.util.LruCache;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.functions.Action1;

public class GitHubInteractor {
  private static LruCache<String, SearchResult> cache = new LruCache<>(5 * 1024 * 1024); // 5MiB

  private GitHubService service;

  public GitHubInteractor() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    service = retrofit.create(GitHubService.class);
  }

  public Observable<SearchResult> searchUsers(final String query) {
    SearchResult cachedResult = cache.get(query);
    if (cachedResult != null) {
      return Observable.just(cachedResult);
    } else {
      return service.searchUsers(query)
          .doOnNext(new Action1<SearchResult>() {
            @Override public void call(SearchResult searchResult) {
              cache.put(query, searchResult);
            }
          });
    }
  }
}
