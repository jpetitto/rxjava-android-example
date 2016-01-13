package com.johnpetitto.rxjavaandroidexample;

import android.util.LruCache;
import retrofit.Retrofit;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class GitHubInteractor {
  private LruCache<String, SearchResult> cache;
  private GitHubService service;

  public GitHubInteractor(Retrofit retrofit, LruCache<String, SearchResult> cache) {
    this.cache = cache;
    service = retrofit.create(GitHubService.class);
  }

  public Observable<SearchResult> searchUsers(final String query) {
    return Observable.concat(cachedResults(query), networkResults(query)).first();
  }

  private Observable<SearchResult> cachedResults(String query) {
    return Observable.just(cache.get(query))
        .filter(new Func1<SearchResult, Boolean>() {
          @Override public Boolean call(SearchResult result) {
            return result != null;
          }
        });
  }

  private Observable<SearchResult> networkResults(final String query) {
    return service.searchUsers(query)
        .doOnNext(new Action1<SearchResult>() {
          @Override public void call(SearchResult result) {
            cache.put(query, result);
          }
        });
  }
}
