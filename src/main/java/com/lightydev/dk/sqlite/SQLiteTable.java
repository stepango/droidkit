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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public abstract class SQLiteTable {

  private static final String WHERE_ID_EQUALS = BaseColumns._ID + "=?";

  public abstract String getTableName();

  protected Cursor select(SQLiteDatabase db, String[] columns, String where, String[] whereArgs, String orderBy) {
    return db.query(getTableName(), columns, where, whereArgs, null, null, orderBy);
  }

  protected long insert(SQLiteDatabase db, ContentValues values) {
    return db.insert(getTableName(), BaseColumns._ID, values);
  }

  long insertById(SQLiteDatabase db, ContentValues values, String id) {
    if (updateById(db, values, id) > 0) {
      return Long.parseLong(id);
    }
    values.put(BaseColumns._ID, id);
    return db.insert(getTableName(), BaseColumns._ID, values);
  }

  protected int delete(SQLiteDatabase db, String where, String[] whereArgs) {
    return db.delete(getTableName(), where, whereArgs);
  }

  int deleteById(SQLiteDatabase db, String id) {
    return delete(db, WHERE_ID_EQUALS, new String[]{id});
  }

  protected int update(SQLiteDatabase db, ContentValues values, String where, String[] whereArgs) {
    return db.update(getTableName(), values, where, whereArgs);
  }

  int updateById(SQLiteDatabase db, ContentValues values, String id) {
    return update(db, values, WHERE_ID_EQUALS, new String[]{id});
  }

  protected Cursor fts(SQLiteDatabase db, String query, String[] columns, String where, String[] whereArgs,
                       String orderBy) {
    throw new UnsupportedOperationException();
  }

  /**
   * @since 2.2.1
   */
  protected void onContentChanged(ContentResolver db) {

  }

  protected final String getFtsTableName() {
    return SQLite.withFtsSfx(getTableName());
  }

  protected List<String> getFtsColumns() {
    throw new UnsupportedOperationException();
  }

  protected abstract void onCreate(SQLiteDatabase db);

  protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    new SQLiteTableEditor(this).drop(db);
    onCreate(db);
  }

}
