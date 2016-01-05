package com.johnpetitto.rxjavaandroidexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding.widget.RxSearchView;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();

  @Bind(R.id.search) SearchView search;
  @Bind(R.id.results) TextView results;

  private Subscription subscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    final GitHubService service = retrofit.create(GitHubService.class);

    subscription = RxSearchView.queryTextChanges(search)
        .observeOn(Schedulers.io())
        .filter(new Func1<CharSequence, Boolean>() {
          @Override public Boolean call(CharSequence charSequence) {
            return charSequence.length() > 0;
          }
        })
        .debounce(2, TimeUnit.SECONDS)
        .switchMap(new Func1<CharSequence, Observable<SearchResult>>() {
          @Override public Observable<SearchResult> call(CharSequence charSequence) {
            return service.searchUsers(charSequence.toString()).doOnUnsubscribe(new Action0() {
              @Override public void call() {
                Log.d(TAG, "inner unsub");
              }
            });
          }
        })
        .map(new Func1<SearchResult, List<SearchItem>>() {
          @Override public List<SearchItem> call(SearchResult searchResult) {
            return searchResult.getItems();
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .doOnUnsubscribe(new Action0() {
          @Override public void call() {
            Log.d(TAG, "outer unsub");
          }
        })
        .subscribe(new Action1<List<SearchItem>>() {
          @Override public void call(List<SearchItem> searchItems) {
            results.setText(searchItems.toString());
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    subscription.unsubscribe();
  }

}
