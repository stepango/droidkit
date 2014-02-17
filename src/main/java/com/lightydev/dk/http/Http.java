/*
 * Copyright 2012-2014 Daniel Serdyukov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightydev.dk.http;

import com.lightydev.dk.DroidKit;
import com.lightydev.dk.http.cache.CacheStore;
import com.lightydev.dk.http.cookie.CookieStore;
import com.lightydev.dk.log.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class Http {

  private static final AtomicInteger SEQUENCE = new AtomicInteger();

  private static final AtomicInteger GUARD = new AtomicInteger();

  private static final Queue<Runnable> REQUEST_CACHE = new ConcurrentLinkedQueue<>();

  private static final AtomicReference<CacheStore> CACHE_STORE = new AtomicReference<>(CacheStore.NO_CACHE);

  private static final AtomicReference<CookieStore> COOKIE_STORE = new AtomicReference<>(CookieStore.MEMORY);

  private static final String URL = "url";

  private Http() {
  }

  public static AsyncHttpEntry get(String url) {
    if (url == null) {
      throw new NullPointerException(URL);
    }
    return new AsyncHttpEntry(AsyncHttpEntry.GET, url, SEQUENCE.incrementAndGet());
  }

  public static AsyncHttpEntry post(String url) {
    if (url == null) {
      throw new NullPointerException(URL);
    }
    return new AsyncHttpEntry(AsyncHttpEntry.POST, url, SEQUENCE.incrementAndGet());
  }

  public static AsyncHttpEntry put(String url) {
    if (url == null) {
      throw new NullPointerException(URL);
    }
    return new AsyncHttpEntry(AsyncHttpEntry.PUT, url, SEQUENCE.incrementAndGet());
  }

  public static AsyncHttpEntry delete(String url) {
    if (url == null) {
      throw new NullPointerException(URL);
    }
    return new AsyncHttpEntry(AsyncHttpEntry.DELETE, url, SEQUENCE.incrementAndGet());
  }

  public interface Header {

    String USER_AGENT = "User-Agent";

    String ACCEPT_ENCODING = "Accept-Encoding";

    String ACCEPT_CHARSET = "Accept-Charset";

    String CONTENT_ENCODING = "Content-Encoding";

    String CONTENT_LENGTH = "Content-Length";

    String CONTENT_TYPE = "Content-Type";

    String COOKIE = "Cookie";

    String IF_MODIFIED_SINCE = "If-Modified-Since";

    String IF_NON_MATCH = "If-None-Match";

  }

  public static final class Engine {

    private static final AtomicBoolean DEBUG_MODE = new AtomicBoolean();

    private Engine() {
    }

    public static void start() {
      if (GUARD.getAndIncrement() == 0 && !REQUEST_CACHE.isEmpty()) {
        DroidKit.EXECUTOR.execute(new Runnable() {
          @Override
          public void run() {
            while (!REQUEST_CACHE.isEmpty()) {
              enqueue(REQUEST_CACHE.poll());
            }
          }
        });
      }
    }

    public static void stop() {
      if (GUARD.decrementAndGet() == 0 && isInDebugMode()) {
        Logger.debug("%s", DroidKit.EXECUTOR);
      }
    }

    public static void setCacheStore(CacheStore cacheStore) {
      CACHE_STORE.set(cacheStore);
    }

    public static void clearCacheStore() {
      CACHE_STORE.get().clear();
    }

    public static void setCookieStore(CookieStore cookieStore) {
      COOKIE_STORE.set(cookieStore);
    }

    public static void clearCookieStore() {
      COOKIE_STORE.get().removeAll();
    }

    public static void setDebugMode(boolean debugMode) {
      DEBUG_MODE.set(debugMode);
    }

    static boolean isInDebugMode() {
      return DEBUG_MODE.get();
    }

    static void enqueue(Runnable entry) {
      if (GUARD.get() > 0) {
        DroidKit.EXECUTOR.execute(entry);
      } else {
        REQUEST_CACHE.offer(entry);
      }
    }

    static CacheStore getCacheStore() {
      return CACHE_STORE.get();
    }

    static CookieStore getCookieStore() {
      return COOKIE_STORE.get();
    }

  }

}
