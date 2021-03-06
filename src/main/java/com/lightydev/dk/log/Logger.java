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

package com.lightydev.dk.log;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class Logger {

  private static final String EX_DIVIDER = "\n======================\n";

  private static final AtomicReference<String> TAG = new AtomicReference<>("DroidKit");

  private static final AtomicBoolean ENABLED = new AtomicBoolean();

  private Logger() {
  }

  public static void setEnabled(String tag, boolean enabled) {
    ENABLED.set(enabled);
    if (!TextUtils.isEmpty(tag)) {
      TAG.set(tag);
    }
  }

  public static void quiet(String format, Object... args) {

  }

  public static void debug(String format, Object... args) {
    if (ENABLED.get()) {
      Log.d(TAG.get(), makeMessage(format, args));
    }
  }

  public static void info(String format, Object... args) {
    if (ENABLED.get()) {
      Log.i(TAG.get(), makeMessage(format, args));
    }
  }

  public static void warn(String format, Object... args) {
    if (ENABLED.get()) {
      Log.w(TAG.get(), makeMessage(format, args));
    }
  }

  public static void error(String format, Object... args) {
    if (ENABLED.get()) {
      Log.e(TAG.get(), makeMessage(format, args));
    }
  }

  public static void error(Throwable e) {
    if (ENABLED.get()) {
      if (e.getCause() == null) {
        Log.e(TAG.get(), e.getMessage() + EX_DIVIDER + TextUtils.join("\n", e.getStackTrace()));
      } else {
        Log.e(TAG.get(), e.getMessage() + EX_DIVIDER + TextUtils.join("\n", e.getCause().getStackTrace()));
      }
    }
  }

  private static String makeMessage(String format, Object... args) {
    final StackTraceElement[] stackTrace = new IOException().fillInStackTrace().getStackTrace();
    String caller = "<unknown>";
    for (final StackTraceElement ste : stackTrace) {
      final String clazz = ste.getClassName();
      if (!clazz.equals(Logger.class.getName())) {
        caller = clazz + "." + ste.getMethodName() + ":" + ste.getLineNumber();
        break;
      }
    }
    final StringBuilder message = new StringBuilder(256)
        .append("[").append(Thread.currentThread().getName()).append("]")
        .append(" ").append(caller).append("\n");
    if (args.length > 0) {
      message.append(String.format(Locale.US, format, args));
    } else {
      message.append(format);
    }
    return message.toString();
  }

}
