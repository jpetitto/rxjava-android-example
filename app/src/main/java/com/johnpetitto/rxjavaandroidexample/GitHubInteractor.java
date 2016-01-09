package com.johnpetitto.rxjavaandroidexample;

import android.util.LruCache;
import retrofit.Retrofit;
import rx.Observable;
import rx.functions.Action1;

public class GitHubInteractor {
  private static final int CACHE_SIZE = 5;
  private LruCache<String, SearchResult> cache;

  private GitHubService service;

  public GitHubInteractor(Retrofit retrofit, LruCache<String, SearchResult> cache) {
    this.cache = cache;
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
