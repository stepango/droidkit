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

package com.lightydev.dk.sqlite;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public abstract class SQLiteConflictClause<T> {

  private static final String ABORT = "ON CONFLICT ABORT";

  private static final String FAIL = "ON CONFLICT FAIL";

  private static final String IGNORE = "ON CONFLICT IGNORE";

  private static final String REPLACE = "ON CONFLICT REPLACE";

  private static final String ROLLBACK = "ON CONFLICT ROLLBACK";

  private final T mDelegate;

  SQLiteConflictClause(T delegate) {
    mDelegate = delegate;
  }

  public T abort() {
    onConflict(ABORT);
    return mDelegate;
  }

  public T fail() {
    onConflict(FAIL);
    return mDelegate;
  }

  public T ignore() {
    onConflict(IGNORE);
    return mDelegate;
  }

  public T replace() {
    onConflict(REPLACE);
    return mDelegate;
  }

  public T rollback() {
    onConflict(ROLLBACK);
    return mDelegate;
  }

  protected abstract void onConflict(String clause);

}
