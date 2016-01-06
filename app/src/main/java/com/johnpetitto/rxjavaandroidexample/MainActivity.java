package com.johnpetitto.rxjavaandroidexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
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
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();

  @Bind(R.id.search_view) SearchView searchView;
  @Bind(R.id.results) RecyclerView results;

  private Subscription subscription;

  // ugly, should use DI in real app
  public static final PublishSubject<String> bus = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    results.setLayoutManager(layoutManager);
    results.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    results.setHasFixedSize(true); // optimization

    bus.subscribe(new Action1<String>() {
      @Override public void call(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
      }
    });

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    final GitHubService service = retrofit.create(GitHubService.class);

    subscription = RxSearchView.queryTextChanges(searchView)
        .observeOn(Schedulers.io())
        .filter(new Func1<CharSequence, Boolean>() {
          @Override public Boolean call(CharSequence charSequence) {
            return charSequence.length() > 0;
          }
        })
        .debounce(2, TimeUnit.SECONDS)
        .switchMap(new Func1<CharSequence, Observable<SearchResult>>() {
          @Override public Observable<SearchResult> call(CharSequence charSequence) {
            return service.searchUsers(charSequence.toString());
          }
        })
        .map(new Func1<SearchResult, List<SearchItem>>() {
          @Override public List<SearchItem> call(SearchResult searchResult) {
            return searchResult.getItems();
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<SearchItem>>() {
          @Override public void call(List<SearchItem> searchItems) {
            results.setAdapter(new SearchRecyclerAdapter(searchItems));
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
