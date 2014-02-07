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
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lightydev.dk.R;
import com.lightydev.dk.content.BitmapLruCache;
import com.lightydev.dk.content.Bitmaps;
import com.lightydev.dk.http.AsyncHttpEntry;
import com.lightydev.dk.http.Http;
import com.lightydev.dk.http.HttpException;
import com.lightydev.dk.http.HttpUtils;
import com.lightydev.dk.http.callback.AsyncHttpCallback;
import com.lightydev.dk.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class RemoteImageView extends ImageView {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(Byte.MAX_VALUE);

  private final int mPlaceholderResId;

  private final int mErrorResId;

  private final Runnable mSetErrorImageCmd = new Runnable() {
    @Override
    public void run() {
      setImageResource(mErrorResId);
    }
  };

  private final int mHwSize;

  private final boolean mUseOptimalSize;

  private final AtomicReference<AsyncHttpEntry> mLoadImageRequest = new AtomicReference<>();

  private final LoadImageCallback mLoadImageCallback = new LoadImageCallback();

  private final SetBitmapCmd mSetBitmapCmd = new SetBitmapCmd();

  private File mCacheDir;

  private AsyncTask<String, Void, Bitmap> mLoadImageTask;

  public RemoteImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RemoteImageView);
    mPlaceholderResId = a.getResourceId(R.styleable.RemoteImageView_placeholder, 0);
    mErrorResId = a.getResourceId(R.styleable.RemoteImageView_errorImage, 0);
    mHwSize = a.getDimensionPixelSize(R.styleable.RemoteImageView_imageHwSize, 0);
    mUseOptimalSize = a.getBoolean(R.styleable.RemoteImageView_optimalSize, true);
    a.recycle();
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      mCacheDir = context.getExternalCacheDir();
    } else {
      mCacheDir = context.getCacheDir();
    }
  }

  public void loadImage(String url) {
    cancelCurrentLoad();
    final String fileName = HttpUtils.getUrlHash(url);
    final Bitmap image = BitmapLruCache.getInstance().get(url);
    if (image != null) {
      setImageBitmap(image);
    } else {
      mLoadImageTask = new LoadImageFromFileTask().execute(url, fileName);
    }
  }

  private void cancelCurrentLoad() {
    if (mLoadImageTask != null) {
      mLoadImageTask.cancel(false);
    }
    if (mLoadImageRequest.get() != null) {
      mLoadImageRequest.get().cancel();
    }
  }

  private int getOptimalWidth() {
    final int width = Math.max(getWidth(), mHwSize);
    if (width > 0 || !mUseOptimalSize) {
      return width;
    }
    return getResources().getDisplayMetrics().widthPixels;
  }

  private int getOptimalHeight() {
    final int height = Math.max(getHeight(), mHwSize);
    if (height > 0 || !mUseOptimalSize) {
      return height;
    }
    return getResources().getDisplayMetrics().heightPixels;
  }

  private final class LoadImageFromFileTask extends AsyncTask<String, Void, Bitmap> {

    @Override
    protected void onPreExecute() {
      setImageResource(mPlaceholderResId);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      final File cacheFile = new File(mCacheDir, params[1]);
      final Bitmap image = Bitmaps.decodeFile(cacheFile.getAbsolutePath(), getOptimalWidth(), getOptimalHeight());
      if (image != null) {
        BitmapLruCache.getInstance().put(params[1], image);
      } else {
        loadImage(params[0], params[1]);
      }
      return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      if (bitmap != null) {
        setImageBitmap(bitmap);
      }
    }

    private void loadImage(String url, String file) {
      mLoadImageCallback.setImageKey(file);
      mLoadImageRequest.set(Http.get(url)
          .setCallback(mLoadImageCallback)
          .setPriority(SEQUENCE.incrementAndGet()));
      mLoadImageRequest.get().send();
    }

  }

  private final class LoadImageCallback implements AsyncHttpCallback {

    private String mImageKey;

    public void setImageKey(String imageKey) {
      mImageKey = imageKey;
      mSetBitmapCmd.setImageKey(imageKey);
    }

    @Override
    public void onSuccess(int statusCode, Map<String, String> headers, InputStream content) {
      final File imageFile = new File(mCacheDir, mImageKey);
      if (imageFile.exists()) {
        final Bitmap image = Bitmaps.decodeFile(imageFile.getAbsolutePath(), getOptimalWidth(), getOptimalHeight());
        if (image != null) {
          BitmapLruCache.getInstance().put(mImageKey, image);
          post(mSetBitmapCmd);
        }
      } else {
        try {
          final FileOutputStream tmpStream = new FileOutputStream(imageFile);
          try {
            IOUtils.copy(content, tmpStream);
          } finally {
            IOUtils.closeQuietly(tmpStream);
            final Bitmap image = Bitmaps.decodeFile(imageFile.getAbsolutePath(), getOptimalWidth(), getOptimalHeight());
            if (image != null) {
              BitmapLruCache.getInstance().put(mImageKey, image);
              post(mSetBitmapCmd);
            }
          }
        } catch (IOException e) {
          onError(new HttpException(HttpException.IO, e));
        }
      }
    }

    @Override
    public void onError(HttpException e) {
      post(mSetErrorImageCmd);
    }

  }

  private final class SetBitmapCmd implements Runnable {

    private String mImageKey;

    public void setImageKey(String imageKey) {
      mImageKey = imageKey;
    }

    @Override
    public void run() {
      removeCallbacks(this);
      setImageBitmap(BitmapLruCache.getInstance().get(mImageKey));
    }

  }

}
