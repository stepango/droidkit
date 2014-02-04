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

package com.lightydev.dk.loader;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.lightydev.dk.http.AsyncHttpEntry;
import com.lightydev.dk.http.HttpException;
import com.lightydev.dk.http.callback.AsyncHttpCallback;
import com.lightydev.dk.log.Logger;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class HttpCursorLoader extends CursorLoader {

  private final AtomicBoolean mUpdate = new AtomicBoolean(true);

  private final Object mWaitLock = new Object();

  private final AtomicReference<AsyncHttpEntry> mHttpRequest = new AtomicReference<>();

  private final AsyncHttpCallback mHttpCallback = new AsyncHttpCallbackImpl();

  public HttpCursorLoader(Context context) {
    super(context);
  }

  public HttpCursorLoader(Context context, Uri uri, String[] columns, String where, String[] whereArgs,
                          String orderBy) {
    super(context, uri, columns, where, whereArgs, orderBy);
  }

  @Override
  public Cursor loadInBackground() {
    if (waitForRequest() && mUpdate.compareAndSet(true, false)) {
      synchronized (mWaitLock) {
        try {
          mWaitLock.wait();
        } catch (InterruptedException e) {
          Logger.error(e);
        }
      }
    }
    return super.loadInBackground();
  }

  @Override
  protected void onForceLoad() {
    if (mUpdate.get()) {
      mHttpRequest.set(newRequest().setCallback(mHttpCallback));
      mHttpRequest.get().send();
    }
    super.onForceLoad();
  }

  @Override
  protected void onReset() {
    cancelHttpRequest();
    super.onReset();
  }

  protected boolean waitForRequest() {
    return false;
  }

  protected abstract AsyncHttpEntry newRequest();

  protected abstract void onAsyncSuccess(int statusCode, Map<String, String> headers, InputStream content);

  protected abstract void onAsyncError(HttpException e);

  private void cancelHttpRequest() {
    try {
      if (mHttpRequest.get() != null) {
        mHttpRequest.get().cancel();
      }
      mHttpRequest.set(null);
    } finally {
      synchronized (mWaitLock) {
        mWaitLock.notifyAll();
      }
    }
  }

  private final class AsyncHttpCallbackImpl implements AsyncHttpCallback {

    @Override
    public void onSuccess(int statusCode, Map<String, String> headers, InputStream content) {
      try {
        onAsyncSuccess(statusCode, headers, content);
      } finally {
        synchronized (mWaitLock) {
          mWaitLock.notifyAll();
        }
      }
    }

    @Override
    public void onError(HttpException e) {
      try {
        onAsyncError(e);
      } finally {
        synchronized (mWaitLock) {
          mWaitLock.notifyAll();
        }
      }
    }

  }

}
