package com.johnpetitto.rxjavaandroidexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    GitHubService service = retrofit.create(GitHubService.class);

    service.searchUsers("jpetitt")
        .subscribeOn(Schedulers.io())
        .subscribe(new Action1<SearchResult>() {
          @Override public void call(SearchResult searchResult) {
            Log.d(TAG, "Search Result: " + searchResult);
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
            if (throwable != null) {
              Log.e(TAG, throwable.getMessage());
            }
          }
        });
  }

}
