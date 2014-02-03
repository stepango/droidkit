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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public abstract class SQLiteFtsTable extends SQLiteTable {

  private static final String FTS_ID_SELECT = BaseColumns._ID + " IN(SELECT docid FROM %1$s WHERE %2$s MATCH %3$s)";

  @Override
  protected long insert(SQLiteDatabase db, ContentValues values) {
    for (final String ftsColumn : getFtsColumns()) {
      final String value = values.getAsString(ftsColumn);
      if (!TextUtils.isEmpty(value)) {
        values.put(SQLite.withFtsSfx(ftsColumn), value.toLowerCase());
      }
    }
    return super.insert(db, values);
  }

  @Override
  protected Cursor fts(SQLiteDatabase db, String query, String[] columns, String where, String[] whereArgs,
                       String orderBy) {
    final List<String> whereIdIn = new ArrayList<>();
    for (final String ftsColumn : getFtsColumns()) {
      whereIdIn.add(String.format(Locale.US, FTS_ID_SELECT, getFtsTableName(), SQLite.withFtsSfx(ftsColumn), query));
    }
    return select(db, columns,
        SQLite.joinWhereWith(SQLite.OP_AND, SQLite.joinWhereWith(SQLite.OP_OR, whereIdIn), where),
        whereArgs, orderBy
    );
  }

  @Override
  protected abstract List<String> getFtsColumns();

}
