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

package com.lightydev.dk;

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class DroidKit {

  public static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

    private final AtomicInteger mSequence = new AtomicInteger();

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "AsyncTask #" + mSequence.incrementAndGet());
    }

  };

  public static final Executor THREAD_POOL_EXECUTOR;

  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      THREAD_POOL_EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;
    } else {
      final int cpuCount = Runtime.getRuntime().availableProcessors();
      THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
          cpuCount + 1, cpuCount * 2 + 1, 1, TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(128), THREAD_FACTORY
      );
    }
  }

  private DroidKit() {
  }

}
