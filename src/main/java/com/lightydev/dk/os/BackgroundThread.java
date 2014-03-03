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

package com.lightydev.dk.os;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class BackgroundThread extends HandlerThread {

  private static final AtomicReference<Handler> HANDLER = new AtomicReference<>();

  BackgroundThread() {
    super("android.bg", android.os.Process.THREAD_PRIORITY_BACKGROUND);
  }

  public static BackgroundThread get() {
    return Holder.INSTANCE;
  }

  public static Handler getHandler() {
    if (HANDLER.get() == null) {
      HANDLER.compareAndSet(null, new Handler(Holder.INSTANCE.getLooper()));
    }
    return HANDLER.get();
  }

  private static final class Holder {

    public static final BackgroundThread INSTANCE = new BackgroundThread();

    static {
      INSTANCE.start();
    }

  }

}
