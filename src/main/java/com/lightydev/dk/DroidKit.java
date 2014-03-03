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

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;

import com.lightydev.dk.concurrent.CpuCoreExecutor;
import com.lightydev.dk.io.ByteArrayPool;
import com.lightydev.dk.io.CharArrayPool;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class DroidKit {

  public static final Executor EXECUTOR = new CpuCoreExecutor();

  private static final AtomicBoolean DEBUG_MODE = new AtomicBoolean();

  private static final AtomicInteger SEQUENCE = new AtomicInteger(9000);

  private DroidKit() {
  }

  public static ProviderInfo getProviderInfo(Context context, Class<? extends ContentProvider> provider, int flags)
      throws PackageManager.NameNotFoundException {
    return context.getPackageManager().getProviderInfo(new ComponentName(
        context.getPackageName(),
        provider.getName()
    ), flags);
  }

  public static void setDebugMode(boolean debugMode) {
    DEBUG_MODE.set(debugMode);
  }

  public static boolean isInDebugMode() {
    return DEBUG_MODE.get();
  }

  public static int nextSequence() {
    return SEQUENCE.incrementAndGet();
  }

  public static void onLowMemory() {
    ByteArrayPool.getInstance().onLowMemory();
    CharArrayPool.getInstance().onLowMemory();
  }

}
