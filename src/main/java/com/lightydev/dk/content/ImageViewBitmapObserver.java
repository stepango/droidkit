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
import android.widget.ImageView;

import com.lightydev.dk.util.Observable;
import com.lightydev.dk.util.Observer;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
class ImageViewBitmapObserver implements Observer<Bitmap> {

  private final ImageView mImageView;

  private final boolean mAutoUnregister;

  ImageViewBitmapObserver(ImageView imageView) {
    this(imageView, true);
  }

  ImageViewBitmapObserver(ImageView imageView, boolean autoUnregister) {
    mImageView = imageView;
    mAutoUnregister = autoUnregister;
  }

  @Override
  public void onChange(Observable<Bitmap> observable, Bitmap bitmap) {
    if (bitmap != null && mAutoUnregister) {
      observable.unregisterObserver(this);
    }
    final Bitmap localBitmap = bitmap;
    mImageView.post(new Runnable() {
      @Override
      public void run() {
        mImageView.setImageBitmap(localBitmap);
      }
    });
  }

}
