package com.johnpetitto.rxjavaandroidexample;

import org.junit.Test;
import rx.observers.TestSubscriber;

public class RxUserBusTest {
  @Test public void singleSub() {
    TestSubscriber<String> subscriber = new TestSubscriber<>();

    RxUserBus.sub().subscribe(subscriber);
    RxUserBus.pub("octocat");

    subscriber.assertValue("octocat");
    subscriber.assertNoErrors();
    subscriber.assertNoTerminalEvent();
  }

  @Test public void multiSub() {
    TestSubscriber<String> subscriberOne = new TestSubscriber<>();
    TestSubscriber<String> subscriberTwo = new TestSubscriber<>();

    RxUserBus.sub().subscribe(subscriberOne);
    RxUserBus.pub("octocat");

    subscriberOne.assertValue("octocat");
    subscriberTwo.assertNoValues();

    RxUserBus.sub().subscribe(subscriberTwo);
    RxUserBus.pub("jpetitto");

    subscriberOne.assertValues("octocat", "jpetitto");
    subscriberTwo.assertValue("jpetitto");
  }
}
