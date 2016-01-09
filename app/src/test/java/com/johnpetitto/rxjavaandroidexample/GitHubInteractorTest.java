package com.johnpetitto.rxjavaandroidexample;

import android.util.LruCache;
import com.google.gson.Gson;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitHubInteractorTest {
  private LruCache<String, SearchResult> cache;

  @SuppressWarnings("unchecked")
  @Before public void setUp() {
    cache = mock(LruCache.class);
    when(cache.get(anyString())).thenReturn(null);
  }

  @Test public void mockedResponseTest() {
    SearchResult result = new SearchResult();
    result.setTotalCount(1);

    SearchItem item = new SearchItem();
    item.setLogin("octocat");
    item.setAvatarUrl("https://avatars.githubusercontent.com/u/583231?v\u003d3");

    result.setItems(Collections.singletonList(item));

    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(new Gson().toJson(result)));

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/search/repositories?q=octocat"))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    TestSubscriber<SearchResult> subscriber = new TestSubscriber<>();
    new GitHubInteractor(retrofit, cache).searchUsers("octocat").subscribe(subscriber);

    subscriber.assertValue(result);
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }

  @Test public void realResponseTest() {
    SearchItem item = new SearchItem();
    item.setLogin("octocat");
    item.setAvatarUrl("https://avatars.githubusercontent.com/u/583231?v\u003d3");

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    TestSubscriber<SearchItem> subscriber = new TestSubscriber<>();

    new GitHubInteractor(retrofit, cache).searchUsers("octocat")
        .flatMap(new Func1<SearchResult, Observable<SearchItem>>() {
          @Override public Observable<SearchItem> call(SearchResult result) {
            return Observable.from(result.getItems());
          }
        })
        .first()
        .toBlocking() // the main difference
        .subscribe(subscriber);

    subscriber.assertValue(item);
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }
}
