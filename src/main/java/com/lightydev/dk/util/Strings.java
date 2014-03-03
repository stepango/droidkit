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

package com.lightydev.dk.util;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class Strings {

  private Strings() {
  }

  public static String joinNonNull(CharSequence delimeter, Iterable<?> tokens) {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final Object token : tokens) {
      if (token != null) {
        if (first) {
          first = false;
        } else {
          sb.append(delimeter);
        }
        sb.append(token);
      }
    }
    return sb.toString();
  }

}
