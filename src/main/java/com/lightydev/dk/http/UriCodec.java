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

import com.lightydev.dk.digest.Hex;

import java.nio.charset.Charset;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public abstract class UriCodec {

  public static final Charset UTF_8 = Charset.forName("UTF-8");

  private static void appendHex(StringBuilder builder, String s, Charset charset) {
    for (byte b : s.getBytes(charset)) {
      builder.append('%').append(Hex.byteToHexString(b, true));
    }
  }

  protected abstract boolean isRetained(char c);

  public final void appendEncoded(StringBuilder builder, String s) {
    appendEncoded(builder, s, UTF_8, false);
  }

  public final void appendPartiallyEncoded(StringBuilder builder, String s) {
    appendEncoded(builder, s, UTF_8, true);
  }

  private void appendEncoded(StringBuilder builder, String string, Charset charset, boolean isPartiallyEncoded) {
    if (string == null) {
      throw new NullPointerException("s == null");
    }
    int escapeStart = -1;
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);
      if (isValidChar(c, isPartiallyEncoded)) {
        if (escapeStart != -1) {
          appendHex(builder, string.substring(escapeStart, i), charset);
          escapeStart = -1;
        }
        if (c == '%' && isPartiallyEncoded) {
          // this is an encoded 3-character sequence like "%20"
          builder.append(string, i, Math.min(i + 3, string.length()));
          i += 2;
        } else if (c == ' ') {
          builder.append('+');
        } else {
          builder.append(c);
        }
      } else if (escapeStart == -1) {
        escapeStart = i;
      }
    }
    if (escapeStart != -1) {
      appendHex(builder, string.substring(escapeStart, string.length()), charset);
    }
  }

  private boolean isValidChar(char c, boolean isPartiallyEncoded) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9')
        || isRetained(c)
        || (c == '%' && isPartiallyEncoded);
  }

}
