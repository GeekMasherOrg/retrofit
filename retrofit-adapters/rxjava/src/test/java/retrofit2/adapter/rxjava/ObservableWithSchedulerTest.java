/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2.adapter.rxjava;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

public final class ObservableWithSchedulerTest {
  @Rule public final MockWebServer server = new MockWebServer();

  interface Service {
    @GET("/") Observable<String> body();
    @GET("/") Observable<Response<String>> response();
    @GET("/") Observable<Result<String>> result();
  }

  private final TestScheduler scheduler = new TestScheduler();
  private Service service;

  @Before public void setUp() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(new StringConverterFactory())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(scheduler))
        .build();
    service = retrofit.create(Service.class);
  }

  @Test public void bodyUsesScheduler() {
    server.enqueue(new MockResponse().setBody("Hi"));

    TestSubscriber<String> subscriber = new TestSubscriber<>();
    service.body().subscribe(subscriber);
    subscriber.assertNoValues();
    subscriber.assertNoTerminalEvent();

    scheduler.triggerActions();
    subscriber.assertValueCount(1);
    subscriber.assertCompleted();
  }

  @Test public void responseUsesScheduler() {
    server.enqueue(new MockResponse().setBody("Hi"));

    TestSubscriber<Response<String>> subscriber = new TestSubscriber<>();
    service.response().subscribe(subscriber);
    subscriber.assertNoValues();
    subscriber.assertNoTerminalEvent();

    scheduler.triggerActions();
    subscriber.assertValueCount(1);
    subscriber.assertCompleted();
  }

  @Test public void resultUsesScheduler() {
    server.enqueue(new MockResponse().setBody("Hi"));

    TestSubscriber<Result<String>> subscriber = new TestSubscriber<>();
    service.result().subscribe(subscriber);
    subscriber.assertNoValues();
    subscriber.assertNoTerminalEvent();

    scheduler.triggerActions();
    subscriber.assertValueCount(1);
    subscriber.assertCompleted();
  }
}