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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class BitmapLruCache {

  private final LruCacheImpl mLruCache;

  BitmapLruCache(LruCacheImpl lruCache) {
    mLruCache = lruCache;
  }

  public static BitmapLruCache getInstance() {
    return Holder.INSTANCE;
  }

  public Bitmap get(String key) {
    return mLruCache.get(key);
  }

  public Bitmap put(String key, Bitmap bitmap) {
    return mLruCache.put(key, bitmap);
  }

  private interface LruCacheImpl {

    Bitmap put(String key, Bitmap value);

    Bitmap get(String key);

  }

  private static final class Holder {

    private static final BitmapLruCache INSTANCE;

    static {
      final int maxSize = (int) (Runtime.getRuntime().maxMemory() / 4);
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
        INSTANCE = new BitmapLruCache(new LruCacheV12(maxSize));
      } else {
        INSTANCE = new BitmapLruCache(new LruCacheV4(maxSize));
      }
    }

  }

  private static final class LruCacheV4 extends android.support.v4.util.LruCache<String, Bitmap>
      implements LruCacheImpl {

    public LruCacheV4(int maxSize) {
      super(maxSize);
    }

    @Override
    protected int sizeOf(String url, Bitmap img) {
      return img.getHeight() * img.getRowBytes();
    }

  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
  private static final class LruCacheV12 extends android.util.LruCache<String, Bitmap>
      implements LruCacheImpl {

    public LruCacheV12(int maxSize) {
      super(maxSize);
    }

    @Override
    protected int sizeOf(String url, Bitmap img) {
      return img.getByteCount();
    }

  }

}
