package com.johnpetitto.rxjavaandroidexample;

import android.util.LruCache;
import com.google.gson.Gson;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.observers.TestSubscriber;

public class GitHubInteractorTest {
  private SearchResult result;
  private Retrofit retrofit;
  private LruCache<String, SearchResult> cache;

  @Before public void setUp() {
    result = new SearchResult();
    result.setTotalCount(1);

    SearchItem item = new SearchItem();
    item.setLogin("octocat");
    item.setAvatarUrl("https://github.com/images/error/octocat_happy.gif");

    result.setItems(Collections.singletonList(item));

    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setBody(new Gson().toJson(result)));

    retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/search/repositories?q=octocat"))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    // noinspection unchecked
    cache = Mockito.mock(LruCache.class);
    Mockito.when(cache.get(Mockito.anyString())).thenReturn(null);
  }

  @Test public void mockedResponseTest() {
    TestSubscriber<SearchResult> subscriber = new TestSubscriber<>();

    new GitHubInteractor(retrofit, cache).searchUsers("octocat").subscribe(subscriber);

    subscriber.assertValue(result);
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }

  @Test public void realResponseTest() {
    TestSubscriber<SearchResult> subscriber = new TestSubscriber<>();

    new GitHubInteractor(retrofit, cache).searchUsers("octocat")
        .toBlocking() // the only difference
        .subscribe(subscriber);

    subscriber.assertValue(result);
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }
}
