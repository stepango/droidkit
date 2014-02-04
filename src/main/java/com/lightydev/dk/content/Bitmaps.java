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
import android.graphics.BitmapFactory;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class Bitmaps {

  private static final double LN_2 = Math.log(2);

  private Bitmaps() {
  }

  public static Bitmap decodeFile(String filePath, int width, int height) {
    if (width > 0 || height > 0) {
      final BitmapFactory.Options ops = new BitmapFactory.Options();
      ops.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(filePath, ops);
      ops.inSampleSize = calculateInSampleSize(ops, width, height);
      ops.inJustDecodeBounds = false;
      return BitmapFactory.decodeFile(filePath, ops);
    }
    return BitmapFactory.decodeFile(filePath);
  }

  private static int calculateInSampleSize(BitmapFactory.Options ops, int width, int height) {
    final int outHeight = ops.outHeight;
    final int outWidth = ops.outWidth;
    if (outHeight > height || outWidth > width) {
      final double ratio = Math.min(
          Math.round((double) outHeight / (double) height),
          Math.round((double) outWidth / (double) width)
      );
      return ratio > 0 ? (int) Math.pow(2, Math.floor(Math.log(ratio) / LN_2)) : 1;
    }
    return 1;
  }

}
