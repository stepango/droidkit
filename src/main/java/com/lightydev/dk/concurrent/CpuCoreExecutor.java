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

import com.lightydev.dk.util.Reflect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class CpuCoreExecutor extends ThreadPoolExecutor {

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  public CpuCoreExecutor() {
    this(new PriorityBlockingQueue<Runnable>(256));
  }

  public CpuCoreExecutor(BlockingQueue<Runnable> workQueue) {
    this(workQueue, Executors.defaultThreadFactory());
  }

  public CpuCoreExecutor(BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(CPU_COUNT + 1, CPU_COUNT * 2 + 1, 10, TimeUnit.SECONDS, workQueue, threadFactory);
  }

  @Override
  public void execute(Runnable command) {
    if (command instanceof Comparable) {
      super.execute(command);
    } else {
      super.execute(new ComparableTask(command));
    }
  }

  private static final class ComparableTask implements Runnable, Comparable<ComparableTask> {

    private final Runnable mDelegate;

    ComparableTask(Runnable delegate) {
      mDelegate = delegate;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!Reflect.classEquals(this, o)) {
        return false;
      }
      final ComparableTask another = (ComparableTask) o;
      return !(mDelegate != null ? !mDelegate.equals(another.mDelegate) : another.mDelegate != null);
    }

    @Override
    public int hashCode() {
      return mDelegate != null ? mDelegate.hashCode() : 0;
    }

    @Override
    public int compareTo(ComparableTask another) {
      return 0;
    }

    @Override
    public void run() {
      mDelegate.run();
    }

  }

}
