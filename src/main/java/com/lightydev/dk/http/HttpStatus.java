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

import android.util.SparseArray;

import com.lightydev.dk.log.Logger;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
final class HttpStatus {

  private static final SparseArray<String> STATUS_ARRAY = new SparseArray<>();

  private static final String HTTP_UNKNOWN = "HTTP_UNKNOWN";

  static {
    final Field[] fields = HttpURLConnection.class.getDeclaredFields();
    for (final Field field : fields) {
      try {
        if (field.getName().startsWith("HTTP_")) {
          final int statusCode = field.getInt(null);
          STATUS_ARRAY.put(statusCode, statusCode + " " + field.getName());
        }
      } catch (IllegalAccessException e) {
        Logger.quiet("%s", e);
      }
    }
  }

  private HttpStatus() {
  }

  public static String getLine(int statusCode) {
    return STATUS_ARRAY.get(statusCode, HTTP_UNKNOWN);
  }

}
