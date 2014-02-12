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

import android.annotation.TargetApi;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class SQLiteDatabaseCompat {

  private static final SQLiteDatabaseVersion SQLITE_DATABASE_IMPL;

  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      SQLITE_DATABASE_IMPL = new SQLiteDatabaseHoneycombImpl();
    } else {
      SQLITE_DATABASE_IMPL = new SQLiteDatabaseLegacyImpl();
    }
  }

  private SQLiteDatabaseCompat() {
  }

  public static void beginTransactionNonExclusive(SQLiteDatabase db) {
    SQLITE_DATABASE_IMPL.beginTransactionNonExclusive(db);
  }

  private interface SQLiteDatabaseVersion {
    void beginTransactionNonExclusive(SQLiteDatabase db);
  }

  private static class SQLiteDatabaseLegacyImpl implements SQLiteDatabaseVersion {

    @Override
    public void beginTransactionNonExclusive(SQLiteDatabase db) {
      db.beginTransaction();
    }

  }

  private static class SQLiteDatabaseHoneycombImpl implements SQLiteDatabaseVersion {

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void beginTransactionNonExclusive(SQLiteDatabase db) {
      db.beginTransactionNonExclusive();
    }

  }

}
