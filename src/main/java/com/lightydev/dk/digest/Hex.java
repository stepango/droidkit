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

package com.lightydev.dk.digest;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class Hex {

  private static final String HEX_CHARS = "0123456789abcdef";

  private Hex() {
  }

  public static String toHexString(byte[] data) {
    final StringBuilder sb = new StringBuilder(data.length * 2);
    for (final byte b : data) {
      final int bit = b & 0xff;
      sb.append(HEX_CHARS.charAt(bit >> 4)).append(HEX_CHARS.charAt(bit & 0xf));
    }
    return sb.toString().trim();
  }

  public static byte[] fromHexString(String hex) {
    final int length = hex.length();
    final byte[] bytes = new byte[length / 2];
    for (int i = 0, k = 0; i + 1 < length; i += 2, k++) {
      bytes[k] = (byte) (Character.digit(hex.charAt(i), 16) << 4);
      bytes[k] += (byte) (Character.digit(hex.charAt(i + 1), 16));
    }
    return bytes;
  }

}
