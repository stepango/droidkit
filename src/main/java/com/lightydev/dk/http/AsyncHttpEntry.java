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

import android.net.Uri;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import com.lightydev.dk.http.body.HttpBody;
import com.lightydev.dk.http.cache.CachePolicy;
import com.lightydev.dk.http.cache.CacheStore;
import com.lightydev.dk.http.callback.AsyncHttpCallback;
import com.lightydev.dk.http.cookie.Cookie;
import com.lightydev.dk.http.cookie.CookieStore;
import com.lightydev.dk.io.IOUtils;
import com.lightydev.dk.log.Logger;
import com.lightydev.dk.util.Reflect;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class AsyncHttpEntry implements Runnable, Comparable<AsyncHttpEntry> {

  static final String GET = "GET";

  static final String POST = "POST";

  static final String PUT = "PUT";

  static final String DELETE = "DELETE";

  private final String mMethod;

  private final int mSequence;

  private final Map<String, String> mHeaders = new ConcurrentHashMap<>();

  private final AtomicReference<HttpBody> mHttpBody = new AtomicReference<>();

  private final AtomicReference<CachePolicy> mCachePolicy = new AtomicReference<>(CachePolicy.DEFAULT);

  private final AtomicReference<AsyncHttpCallback> mCallback = new AtomicReference<>();

  private final AtomicReference<RetryPolicy> mRetryPolicy = new AtomicReference<>(RetryPolicy.DEFAULT);

  private final AtomicInteger mRetry = new AtomicInteger();

  private final AtomicInteger mPriority = new AtomicInteger();

  private final AtomicBoolean mCanceled = new AtomicBoolean();

  private String mUrl;

  public AsyncHttpEntry(String method, String url, int sequence) {
    mMethod = method;
    mSequence = sequence;
    mUrl = url;
  }

  public AsyncHttpEntry setBody(HttpBody body) {
    switch (mMethod) {
      case GET:
        appendBodyToUrl(body);
        return this;
      case DELETE:
        appendBodyToUrl(body);
        setCachePolicy(CachePolicy.NO_CACHE);
        return this;
      default:
        mHttpBody.set(body);
        setCachePolicy(CachePolicy.NO_CACHE);
        return this;
    }
  }

  public AsyncHttpEntry setCallback(AsyncHttpCallback cb) {
    mCallback.set(cb);
    return this;
  }

  public AsyncHttpEntry addHeader(String key, String value) {
    mHeaders.put(key, value);
    return this;
  }

  public AsyncHttpEntry addHeaders(Map<String, String> headers) {
    mHeaders.putAll(headers);
    return this;
  }

  public AsyncHttpEntry setCachePolicy(CachePolicy cachePolicy) {
    mCachePolicy.set(cachePolicy);
    return this;
  }

  public AsyncHttpEntry setRetryPolicy(RetryPolicy retryPolicy) {
    mRetryPolicy.set(retryPolicy);
    return this;
  }

  public AsyncHttpEntry setPriority(int priority) {
    mPriority.set(priority);
    return this;
  }

  public void send() {
    mCanceled.set(false);
    Http.Engine.enqueue(this);
  }

  public void cancel() {
    mCanceled.compareAndSet(false, true);
  }

  @Override
  public void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    if (!mCanceled.get()) {
      final long start = SystemClock.uptimeMillis();
      final int statusCode;
      if (mCachePolicy.get().ignoreCache(mUrl)) {
        statusCode = performNetworkRequest(null, -1);
      } else {
        if (Http.Engine.getCacheStore().contains(mUrl)) {
          statusCode = performCacheRequest();
        } else {
          statusCode = performNetworkRequest(null, -1);
        }
      }
      if (Http.Engine.isInDebugMode()) {
        Logger.debug("%s [%s] in %d ms.", this, HttpStatus.getLine(statusCode), (SystemClock.uptimeMillis() - start));
      }
    }
  }

  @Override
  public int compareTo(AsyncHttpEntry another) {
    if (this.mPriority.get() == another.mPriority.get()) {
      return this.mSequence - another.mSequence;
    }
    return another.mPriority.get() - this.mPriority.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!Reflect.classEquals(this, o)) {
      return false;
    }
    final AsyncHttpEntry another = (AsyncHttpEntry) o;
    return TextUtils.equals(mUrl, another.mUrl)
        && TextUtils.equals(mMethod, another.mMethod)
        && mSequence == another.mSequence;
  }

  @Override
  public int hashCode() {
    int result = Reflect.hashCode(mMethod);
    result = 31 * result + mSequence;
    result = 31 * result + Reflect.hashCode(mUrl);
    return result;
  }

  @Override
  public String toString() {
    return "#" + mSequence + " " + mMethod + " " + mUrl;
  }

  private void appendBodyToUrl(HttpBody body) {
    if (mUrl.contains(HttpBody.QUERY_SEPARATOR)) {
      mUrl += (HttpBody.PARAMETER_SEPARATOR + body);
    } else {
      mUrl += (HttpBody.QUERY_SEPARATOR + body);
    }
  }

  private HttpURLConnection openConnection(String url) throws IOException {
    final HttpURLConnection cn = (HttpURLConnection) new URL(HttpUtils.toAsciiUrl(url)).openConnection();
    cn.setRequestMethod(mMethod);
    return cn;
  }

  private int performCacheRequest() {
    final CacheStore.Entry cacheEntry = Http.Engine.getCacheStore().get(mUrl);
    if (cacheEntry == null) {
      return performNetworkRequest(null, -1);
    } else if (cacheEntry.isExpired()) {
      return performNetworkRequest(cacheEntry.getETag(), cacheEntry.getLastModified());
    } else {
      try {
        onSuccessIfCallbackExists(
            HttpURLConnection.HTTP_NOT_MODIFIED,
            Collections.<String, String>emptyMap(),
            cacheEntry.getContent()
        );
        return HttpURLConnection.HTTP_NOT_MODIFIED;
      } catch (IOException e) {
        onError(new HttpException(HttpException.CACHE, mUrl, e));
      }
    }
    return -1;
  }

  private int performNetworkRequest(String etag, long lastModified) {
    try {
      final HttpURLConnection cn = openConnection(mUrl);
      try {
        setConnectionCookies(cn);
        setConnectionHeaders(cn, etag, lastModified);
        sendBodyIfExists(cn);
        return onSuccess(cn.getResponseCode(), HttpUtils.readHeaders(cn), HttpUtils.readContent(cn));
      } finally {
        cn.disconnect();
      }
    } catch (IOException e) {
      onError(new HttpException(HttpException.NETWORK, mUrl, e));
    }
    return -1;
  }

  private void setConnectionCookies(HttpURLConnection cn) {
    final List<Cookie> cookies = Http.Engine.getCookieStore().get(Uri.parse(mUrl));
    for (final Cookie cookie : cookies) {
      cn.addRequestProperty(Http.Header.COOKIE, cookie.toString());
    }
  }

  private void setConnectionHeaders(HttpURLConnection cn, String etag, long lastModified) {
    if (!TextUtils.isEmpty(etag)) {
      cn.setRequestProperty(Http.Header.IF_NON_MATCH, etag);
    }
    if (lastModified > 0) {
      cn.setRequestProperty(Http.Header.IF_MODIFIED_SINCE, HttpDate.format(lastModified));
    }
    for (final Map.Entry<String, String> header : mHeaders.entrySet()) {
      cn.setRequestProperty(header.getKey(), header.getValue());
    }
  }

  private void sendBodyIfExists(HttpURLConnection cn) throws IOException {
    if (mHttpBody.get() != null) {
      mHttpBody.get().writeTo(cn);
    }
  }

  private int onSuccess(int statusCode, Map<String, String> headers, InputStream content) {
    Http.Engine.getCookieStore().add(Uri.parse(mUrl), Cookie.parse(headers.get(CookieStore.Header.SET_COOKIE)));
    final CachePolicy cacheStorePolicy = Http.Engine.getCacheStore().getPolicy();
    if (cacheStorePolicy.shouldCache(mUrl, headers) && mCachePolicy.get().shouldCache(mUrl, headers)) {
      if (statusCode == HttpURLConnection.HTTP_OK) {
        onSuccessWithCacheSave(headers, content);
      } else if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
        onSuccessWithCacheUpdate(headers);
      } else {
        onSuccessIfCallbackExists(statusCode, headers, content);
      }
    } else {
      onSuccessIfCallbackExists(statusCode, headers, content);
    }
    return statusCode;
  }

  private void onSuccessWithCacheSave(Map<String, String> headers, InputStream content) {
    final CacheStore.Entry cacheEntry = Http.Engine.getCacheStore().put(mUrl, headers, content);
    if (cacheEntry != null) {
      try {
        onSuccessIfCallbackExists(HttpURLConnection.HTTP_OK, headers, cacheEntry.getContent());
      } catch (IOException e) {
        onError(new HttpException(HttpException.CACHE, mUrl, e));
      }
    }
  }

  private void onSuccessWithCacheUpdate(Map<String, String> headers) {
    final CacheStore.Entry cacheEntry = Http.Engine.getCacheStore().update(mUrl, headers);
    if (cacheEntry != null) {
      try {
        onSuccessIfCallbackExists(HttpURLConnection.HTTP_NOT_MODIFIED, headers, cacheEntry.getContent());
      } catch (IOException e) {
        onError(new HttpException(HttpException.CACHE, mUrl, e));
      }
    }
  }

  private void onSuccessIfCallbackExists(int statusCode, Map<String, String> headers, InputStream content) {
    try {
      if (mCallback.get() != null) {
        mCallback.get().onSuccess(statusCode, headers, content);
      }
    } finally {
      IOUtils.closeQuietly(content);
    }
  }

  private void onError(HttpException e) {
    if (Http.Engine.isInDebugMode()) {
      Logger.error(e.getCause());
    }
    if (mRetryPolicy.get().shouldRetry(mRetry.getAndIncrement(), e)) {
      Http.Engine.enqueue(this);
    } else if (mCallback.get() != null) {
      mCallback.get().onError(e);
    }
  }

}
