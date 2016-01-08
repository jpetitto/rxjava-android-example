package com.johnpetitto.rxjavaandroidexample;

import java.util.HashMap;
import java.util.Map;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.functions.Action1;

public class GitHubInteractor {
  private static final int CACHE_SIZE = 5;
  private Map<String, SearchResult> cache = new HashMap<>(CACHE_SIZE);

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
              addToCache(query, searchResult);
            }
          });
    }
  }

  private void addToCache(String query, SearchResult result) {
    if (cache.size() == 5) {
      cache.clear(); // dumb, but LruCache is stubbed and I'm lazy
    }
    cache.put(query, result);
  }
}
