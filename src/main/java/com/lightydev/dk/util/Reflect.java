/*
 * Copyright 2012-2013 Daniel Serdyukov
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
 * @version 1.0
 */
public final class Reflect {

  private Reflect() {
  }

  public static boolean classEquals(Object o1, Object o2) {
    return o1 != null && o2 != null && o1.getClass() == o2.getClass();
  }

  public static int hashCode(Object o) {
    if (o != null) {
      return o.hashCode();
    }
    return 0;
  }

}
