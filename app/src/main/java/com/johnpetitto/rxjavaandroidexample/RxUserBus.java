package com.johnpetitto.rxjavaandroidexample;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class RxUserBus {
  private static PublishSubject<String> bus = PublishSubject.create();

  private RxUserBus() {}

  public static Observable<String> sub() {
    return bus.asObservable();
  }

  public static void pub(String user) {
    bus.onNext(user);
  }
}
