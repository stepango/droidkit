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

package com.lightydev.dk.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class CpuCoreExecutor extends ThreadPoolExecutor {

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  public CpuCoreExecutor() {
    this(new LinkedBlockingQueue<Runnable>(128));
  }

  public CpuCoreExecutor(BlockingQueue<Runnable> workQueue) {
    this(workQueue, Executors.defaultThreadFactory());
  }

  public CpuCoreExecutor(BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(CPU_COUNT + 1, CPU_COUNT * 2 + 1, 10, TimeUnit.SECONDS, workQueue, threadFactory);
  }

}
