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

package com.lightydev.dk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lightydev.dk.R;
import com.lightydev.dk.content.ImageLoader;
import com.lightydev.dk.util.Observable;
import com.lightydev.dk.util.Observer;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class DkImageView extends ImageView {

  private final SetImageCmd mSetImageCmd = new SetImageCmd();

  private final SetErrorImageCmd mSetErrorImageCmd = new SetErrorImageCmd();

  private final Observer<Bitmap> mObserver = new Observer<Bitmap>() {
    @Override
    public void onChange(Observable<Bitmap> observable, Bitmap bitmap) {
      if (bitmap != null) {
        observable.unregisterObserver(this);
        mSetImageCmd.setBitmap(bitmap);
        if (Looper.myLooper() != Looper.getMainLooper()) {
          post(mSetImageCmd);
        } else {
          mSetImageCmd.run();
        }
      } else {
        if (Looper.myLooper() != Looper.getMainLooper()) {
          post(mSetErrorImageCmd);
        } else {
          mSetErrorImageCmd.run();
        }
      }
    }
  };

  private int mHwSize;

  private boolean mUseOptimalSize;

  private Uri mImageUri;

  public DkImageView(Context context) {
    this(context, null);
  }

  public DkImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DkImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DkImageView, defStyle, 0);
    final int errorResId = a.getResourceId(R.styleable.DkImageView_errorImage, 0);
    mHwSize = a.getInteger(R.styleable.DkImageView_imageHwSize, 0);
    mUseOptimalSize = a.getBoolean(R.styleable.DkImageView_optimalSize, true);
    a.recycle();
    mSetErrorImageCmd.setResId(errorResId);
  }

  public void loadImage(Uri uri) {
    if (mImageUri != uri) {
      unregisterObserver();
      mImageUri = uri;
      ImageLoader.registerObserver(mImageUri, mObserver);
      ImageLoader.loadImage(mImageUri);
    }
  }

  public void setHwSize(int hwSize) {
    mHwSize = hwSize;
  }

  public void setUseOptimalSize(boolean useOptimalSize) {
    mUseOptimalSize = useOptimalSize;
  }

  public void setErrorResId(int resId) {
    mSetErrorImageCmd.setResId(resId);
  }

  protected void onImageReady(Bitmap bitmap) {
    setImageBitmap(bitmap);
  }

  protected void onImageError(int errorResId) {
    setImageResource(errorResId);
  }

  @Override
  protected void onDetachedFromWindow() {
    unregisterObserver();
    super.onDetachedFromWindow();
  }

  private void unregisterObserver() {
    if (mImageUri != null) {
      ImageLoader.unregisterObserver(mImageUri, mObserver);
    }
    removeCallbacks(mSetImageCmd);
  }

  private final class SetImageCmd implements Runnable {

    private Bitmap mBitmap;

    public void setBitmap(Bitmap bitmap) {
      mBitmap = bitmap;
    }

    @Override
    public void run() {
      removeCallbacks(this);
      onImageReady(mBitmap);
    }

  }

  private final class SetErrorImageCmd implements Runnable {

    private int mErrorResId;

    public void setResId(int errorResId) {
      mErrorResId = errorResId;
    }

    @Override
    public void run() {
      if (mErrorResId > 0) {
        onImageError(mErrorResId);
      }
    }

  }

}
