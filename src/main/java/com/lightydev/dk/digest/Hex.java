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

  private static final char[] DIGITS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
      'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
      'u', 'v', 'w', 'x', 'y', 'z'
  };

  private static final char[] UPPER_CASE_DIGITS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
      'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
      'U', 'V', 'W', 'X', 'Y', 'Z'
  };

  private Hex() {
  }

  public static String toHexString(byte[] data) {
    return toHexString(data, false);
  }

  public static String toHexString(byte[] data, boolean upperCase) {
    final StringBuilder sb = new StringBuilder(data.length * 2);
    for (final byte b : data) {
      sb.append(byteToHexString(b, upperCase));
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

  public static String byteToHexString(byte b, boolean upperCase) {
    final char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
    final char[] buf = new char[]{digits[(b >> 4) & 0xf], digits[b & 0xf]};
    return new String(buf, 0, 2);
  }

}
