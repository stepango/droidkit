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
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.lightydev.dk.http.Http;
import com.lightydev.dk.http.HttpException;
import com.lightydev.dk.http.HttpUtils;
import com.lightydev.dk.http.callback.AsyncHttpCallback;
import com.lightydev.dk.log.Logger;
import com.lightydev.dk.util.Observable;
import com.lightydev.dk.util.Observer;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class ImageLoader {

  public static final String FILE_SCHEME = "file";

  public static final List<String> URL_SCHEME = new ArrayList<>();

  static {
    URL_SCHEME.add("http");
    URL_SCHEME.add("https");
  }

  private static final Map<Uri, Observable<Bitmap>> OBSERVABLES = new ConcurrentHashMap<>();

  private static final Set<Uri> IMAGE_QUEUE = new ConcurrentSkipListSet<>();

  private ImageLoader() {
  }

  public static void loadImage(Uri uri) {
    loadImage(uri, -1);
  }

  public static void loadImage(Uri uri, ImageView imageView) {
    registerObserver(uri, new ImageViewBitmapObserver(imageView));
    final int hwSize = Math.max(imageView.getWidth(), imageView.getHeight());
    if (hwSize > 0) {
      loadImage(uri, hwSize);
    } else {
      loadImage(uri, imageView.getContext().getResources().getDisplayMetrics());
    }
  }

  public static void loadImage(Uri uri, DisplayMetrics dm) {
    loadImage(uri, Math.max(dm.widthPixels, dm.heightPixels));
  }

  public static void loadImage(Uri uri, int hwSize) {
    if (uri == null) {
      throw new NullPointerException("uri");
    }
    final String scheme = uri.getScheme();
    final String key = HttpUtils.getUrlHash(uri.toString());
    final Bitmap bitmap = BitmapLruCache.getInstance().get(key);
    if (bitmap != null) {
      notifyObservers(uri, bitmap);
    } else if (TextUtils.equals(FILE_SCHEME, scheme)) {
      loadFile(uri, key, hwSize);
    } else if (URL_SCHEME.contains(scheme)) {
      loadUrl(uri, key, hwSize);
    }
  }

  public static void registerObserver(Uri uri, Observer<Bitmap> observer) {
    getObservableForUri(uri).registerObserver(observer);
  }

  public static void unregisterObserver(Uri uri, Observer<Bitmap> observer) {
    getObservableForUri(uri).unregisterObserver(observer);
  }

  public static void unregisterAll(Uri uri) {
    getObservableForUri(uri).unregisterAll();
  }

  public static void notifyObservers(Uri uri, Bitmap bitmap) {
    getObservableForUri(uri).notifyObservers(bitmap);
  }

  private static Observable<Bitmap> getObservableForUri(Uri uri) {
    Observable<Bitmap> observable = OBSERVABLES.get(uri);
    if (observable == null) {
      observable = new Observable<>();
      OBSERVABLES.put(uri, observable);
    }
    return observable;
  }

  private static void loadFile(Uri uri, String key, int hwSize) {
    if (IMAGE_QUEUE.add(uri)) {
      new LoadImageTask(uri, hwSize).execute(uri.getPath(), key);
    }
  }

  private static void loadUrl(Uri uri, String key, int hwSize) {
    if (IMAGE_QUEUE.add(uri)) {
      Http.get(uri.toString()).setCallback(new LoadImageCallback(uri, key, hwSize)).send();
    }
  }

  private static final class LoadImageCallback implements AsyncHttpCallback {

    private final Uri mUri;

    private final String mKey;

    private final int mHwSize;

    private LoadImageCallback(Uri uri, String key, int hwSize) {
      mUri = uri;
      mKey = key;
      mHwSize = hwSize;
    }

    @Override
    public void onSuccess(int statusCode, Map<String, String> headers, InputStream content) {
      if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
        final Bitmap bitmap = Bitmaps.decodeStream(content, mHwSize);
        if (bitmap != null) {
          BitmapLruCache.getInstance().put(mKey, bitmap);
        }
        ImageLoader.notifyObservers(mUri, bitmap);
      }
      IMAGE_QUEUE.remove(mUri);
    }

    @Override
    public void onError(HttpException e) {
      Logger.error(e);
      ImageLoader.notifyObservers(mUri, null);
      IMAGE_QUEUE.remove(mUri);
    }
  }

  private static final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

    private final Uri mUri;

    private final int mHwSize;

    private LoadImageTask(Uri uri, int hwSize) {
      mUri = uri;
      mHwSize = hwSize;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      final Bitmap bitmap = Bitmaps.decodeFile(params[0], mHwSize);
      if (bitmap != null) {
        BitmapLruCache.getInstance().put(params[1], bitmap);
      }
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      ImageLoader.notifyObservers(mUri, bitmap);
      IMAGE_QUEUE.remove(mUri);
    }

  }

}
