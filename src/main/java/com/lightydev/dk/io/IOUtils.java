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

package com.lightydev.dk.io;

import com.lightydev.dk.log.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class IOUtils {

  public static final int EOF = -1;

  public static final int BUFFER_SIZE = 64 * 1024;

  private IOUtils() {
  }

  public static void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (IOException e) {
      Logger.quiet("%s", e);
    }
  }

  public static String toString(InputStream is) throws IOException {
    final StringBuilder result = new StringBuilder();
    final Reader reader = new InputStreamReader(is, Charset.defaultCharset());
    final char[] buffer = CharArrayPool.getInstance().obtain();
    try {
      int bytes;
      while ((bytes = reader.read(buffer)) != EOF) {
        result.append(buffer, 0, bytes);
      }
    } finally {
      CharArrayPool.getInstance().free(buffer);
      closeQuietly(reader);
    }
    return result.toString();
  }

  public static String toStringQuietly(InputStream is) {
    try {
      return toString(is);
    } catch (IOException e) {
      return "";
    }
  }

  public static int copy(InputStream source, OutputStream destination) throws IOException {
    int totalBytes = 0;
    final byte[] buffer = ByteArrayPool.getInstance().obtain();
    try {
      int bytes;
      while ((bytes = source.read(buffer)) != EOF) {
        destination.write(buffer, 0, bytes);
        totalBytes += bytes;
      }
    } finally {
      ByteArrayPool.getInstance().free(buffer);
    }
    return totalBytes;
  }

  public static int copyQuietly(InputStream source, OutputStream destination) {
    try {
      return copy(source, destination);
    } catch (IOException e) {
      return 0;
    }
  }

}
