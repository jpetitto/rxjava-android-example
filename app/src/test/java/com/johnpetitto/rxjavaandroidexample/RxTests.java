package com.johnpetitto.rxjavaandroidexample;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import rx.Observable;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public class RxTests {
  @Test public void subscriberTest() {
    TestSubscriber<Integer> subscriber = new TestSubscriber<>();

    Observable.just(1, 2, 3)
        .limit(3)
        .subscribe(subscriber);

    subscriber.assertValues(1, 2, 3);
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }

  @Test public void schedulerTest() {
    TestScheduler scheduler = Schedulers.test();
    TestSubscriber<Long> subscriber = new TestSubscriber<>();

    Observable.interval(1, TimeUnit.SECONDS, scheduler)
        .limit(3)
        .subscribe(subscriber);

    subscriber.assertNoValues();

    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
    subscriber.assertValues(0L);

    scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
    subscriber.assertValues(0L, 1L, 2L);
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }

  @Test public void testGitHub() {
    TestSubscriber<String> subscriber = new TestSubscriber<>();
    new GitHubInteractor().searchUsers("octocat")
        .flatMap(new Func1<SearchResult, Observable<SearchItem>>() {
          @Override public Observable<SearchItem> call(SearchResult searchResult) {
            return Observable.from(searchResult.getItems());
          }
        })
        .map(new Func1<SearchItem, String>() {
          @Override public String call(SearchItem searchItem) {
            return searchItem.getLogin();
          }
        })
        .first()
        .toBlocking()
        .subscribe(subscriber);

    subscriber.assertValue("octocat");
    subscriber.assertNoErrors();
    subscriber.assertCompleted();
  }
}
