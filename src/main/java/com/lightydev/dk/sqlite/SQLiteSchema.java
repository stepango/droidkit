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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SQLiteSchema {

  private final String mName;

  private final int mVersion;

  private final Map<String, SQLiteTable> mSchema = new ConcurrentHashMap<>();

  public SQLiteSchema(String name, int version) {
    mName = name;
    mVersion = version;
  }

  public SQLiteSchema addTable(SQLiteTable table) {
    mSchema.put(table.getTableName(), table);
    return this;
  }

  public String getName() {
    return mName;
  }

  public int getVersion() {
    return mVersion;
  }

  void onCreate(SQLiteDatabase db) {
    for (final SQLiteTable table : mSchema.values()) {
      table.onCreate(db);
    }
  }

  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    for (final SQLiteTable table : mSchema.values()) {
      table.onUpgrade(db, oldVersion, newVersion);
    }
  }

  SQLiteTable acquireTable(Uri uri) {
    final String tableName = uri.getPathSegments().get(0);
    final SQLiteTable table = mSchema.get(tableName);
    if (table == null) {
      throw new SQLiteException("no such table: " + table);
    }
    return table;
  }

}
