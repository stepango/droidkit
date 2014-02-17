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

package com.lightydev.dk.content;

import android.graphics.Bitmap;
import android.os.Handler;

import com.lightydev.dk.util.Observable;
import com.lightydev.dk.util.Observer;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public abstract class BitmapObserver implements Observer<Bitmap> {

  private final Handler mHandler;

  private final boolean mAutoUnregister;

  public BitmapObserver(Handler handler) {
    this(handler, true);
  }

  public BitmapObserver(Handler handler, boolean autoUnregister) {
    mHandler = handler;
    mAutoUnregister = autoUnregister;
  }

  @Override
  public void onChange(Observable<Bitmap> observable, Bitmap data) {
    if (data != null && mAutoUnregister) {
      observable.unregisterObserver(this);
    }
    if (mHandler != null) {
      final Bitmap localBitmap = data;
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          onBitmapReady(localBitmap);
        }
      });
    } else {
      onBitmapReady(data);
    }
  }

  protected abstract void onBitmapReady(Bitmap bitmap);

}
