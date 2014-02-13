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

package com.lightydev.dk.http.cookie;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.lightydev.dk.sqlite.SQLiteColumn;
import com.lightydev.dk.sqlite.SQLiteTable;
import com.lightydev.dk.sqlite.SQLiteTableEditor;
import com.lightydev.dk.sqlite.SQLiteUniqueKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.1
 */
public class SQLiteCookie extends SQLiteTable {

  private final String mTableName;

  public SQLiteCookie(String tableName) {
    super();
    mTableName = tableName;
  }

  public static ContentValues toContentValues(String host, Cookie cookie) {
    final ContentValues values = new ContentValues();
    values.put(Columns.NAME, cookie.getName());
    values.put(Columns.VALUE, cookie.getValue());
    values.put(Columns.HOST, host);
    values.put(Columns.LAST_MODIFIED, cookie.getLastModified());
    values.put(Columns.MAX_AGE, cookie.getMaxAge());
    return values;
  }

  public static List<Cookie> toCookieJar(Cursor c) {
    final List<Cookie> cookies = new ArrayList<>(c.getCount());
    do {
      final Cookie cookie = new Cookie(
          c.getString(c.getColumnIndex(Columns.NAME)),
          c.getString(c.getColumnIndex(Columns.VALUE))
      );
      cookie.setLastModified(c.getLong(c.getColumnIndex(Columns.LAST_MODIFIED)));
      cookie.setLastModified(c.getLong(c.getColumnIndex(Columns.MAX_AGE)));
      cookies.add(cookie);
    } while (c.moveToNext());
    return cookies;
  }

  @Override
  public String getTableName() {
    return mTableName;
  }

  @Override
  protected void onCreate(SQLiteDatabase db) {
    new SQLiteTableEditor(this)
        .addColumn(new SQLiteColumn(Columns.NAME))
        .addColumn(new SQLiteColumn(Columns.VALUE))
        .addColumn(new SQLiteColumn(Columns.HOST))
        .addColumn(new SQLiteColumn(Columns.LAST_MODIFIED).integer())
        .addColumn(new SQLiteColumn(Columns.MAX_AGE).integer())
        .addUniqueKey(new SQLiteUniqueKey(Columns.HOST, Columns.NAME).onConflict().replace())
        .create(db);
  }

  public interface Columns extends BaseColumns {
    String NAME = "name";
    String VALUE = "value";
    String HOST = "host";
    String LAST_MODIFIED = "last_modified";
    String MAX_AGE = "max_age";
  }

}
