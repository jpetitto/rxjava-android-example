package com.johnpetitto.rxjavaandroidexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding.widget.RxTextView;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
  @Bind(R.id.search_bar) EditText searchBar;
  @Bind(R.id.results) RecyclerView results;

  private CompositeSubscription subs = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    results.setLayoutManager(layoutManager);
    results.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    results.setHasFixedSize(true); // optimization

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    LruCache<String, SearchResult> cache = new LruCache<>(5 * 1024 * 1024); // 5MiB

    final GitHubInteractor interactor = new GitHubInteractor(retrofit, cache);

    subs.add(RxUserBus.sub().subscribe(new Action1<String>() {
      @Override public void call(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
      }
    }));

    subs.add(RxTextView.textChanges(searchBar)
        .observeOn(Schedulers.io())
        .skip(1)
        .debounce(1, TimeUnit.SECONDS)
        .filter(new Func1<CharSequence, Boolean>() {
          @Override public Boolean call(CharSequence charSequence) {
            return charSequence.length() > 0;
          }
        })
        .switchMap(new Func1<CharSequence, Observable<SearchResult>>() {
          @Override public Observable<SearchResult> call(CharSequence charSequence) {
            return interactor.searchUsers(charSequence.toString());
          }
        })
        .flatMap(new Func1<SearchResult, Observable<List<SearchItem>>>() {
          @Override public Observable<List<SearchItem>> call(SearchResult searchResult) {
            return Observable.from(searchResult.getItems())
                .limit(20)
                .toList();
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<SearchItem>>() {
          @Override public void call(List<SearchItem> searchItems) {
            results.setAdapter(new SearchRecyclerAdapter(searchItems));
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
            Log.e(MainActivity.class.getName(), throwable.getMessage());
          }
        }));
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    subs.unsubscribe();
  }
}
