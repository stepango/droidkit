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

package com.lightydev.dk.util;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class Observable<T> {

  private final Set<Observer<T>> mObservers = new CopyOnWriteArraySet<>();

  public boolean registerObserver(Observer<T> observer) {
    if (observer == null) {
      throw new IllegalArgumentException("The observer is null.");
    }
    return mObservers.add(observer);
  }

  public boolean unregisterObserver(Observer<T> observer) {
    if (observer == null) {
      throw new IllegalArgumentException("The observer is null.");
    }
    return mObservers.remove(observer);
  }

  public void unregisterAll() {
    mObservers.clear();
  }

  public void notifyObservers(T data) {
    for (final Observer<T> observer : mObservers) {
      observer.onChange(this, data);
    }
  }

}
