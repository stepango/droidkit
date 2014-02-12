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

package com.lightydev.dk.loader.v4;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.Loader;

import com.lightydev.dk.http.AsyncHttpEntry;
import com.lightydev.dk.http.HttpException;
import com.lightydev.dk.http.callback.AsyncHttpCallback;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public abstract class AsyncHttpLoader<D> extends Loader<D> {

  private final Handler mHandler = new Handler();

  private final AtomicReference<D> mResult = new AtomicReference<>();

  private final AtomicReference<AsyncHttpEntry> mHttpRequest = new AtomicReference<>();

  private final AsyncHttpCallback mHttpCallback = new AsyncHttpCallbackImpl();

  private final Runnable mDeliverResultCmd = new Runnable() {
    @Override
    public void run() {
      mHandler.removeCallbacks(this);
      deliverResult(mResult.get());
    }
  };

  private final Runnable mResetCmd = new Runnable() {
    @Override
    public void run() {
      mHandler.removeCallbacks(this);
      reset();
    }
  };

  public AsyncHttpLoader(Context context) {
    super(context);
  }

  @Override
  public void deliverResult(D data) {
    if (isReset()) {
      mResult.set(null);
    } else if (isStarted()) {
      super.deliverResult(data);
    }
  }

  @Override
  protected void onStartLoading() {
    if (mResult.get() == null) {
      forceLoad();
    } else {
      deliverResult(mResult.get());
    }
  }

  @Override
  protected void onForceLoad() {
    if (mHttpRequest.get() != null) {
      mHttpRequest.get().cancel();
    }
    mHttpRequest.set(newRequest().setCallback(mHttpCallback));
    mHttpRequest.get().send();
  }

  @Override
  protected void onReset() {
    if (mHttpRequest.get() != null) {
      mHttpRequest.get().cancel();
    }
    mHttpRequest.set(null);
    mResult.set(null);
  }

  protected abstract AsyncHttpEntry newRequest();

  protected abstract D onAsyncSuccess(int statusCode, Map<String, String> headers, InputStream content);

  protected abstract D onAsyncError(HttpException e);

  private final class AsyncHttpCallbackImpl implements AsyncHttpCallback {

    @Override
    public void onSuccess(int statusCode, Map<String, String> headers, InputStream content) {
      mResult.set(onAsyncSuccess(statusCode, headers, content));
      mHandler.post(mDeliverResultCmd);
    }

    @Override
    public void onError(HttpException e) {
      mResult.set(onAsyncError(e));
      mHandler.post(mDeliverResultCmd);
      mHandler.post(mResetCmd);
    }

  }

}
