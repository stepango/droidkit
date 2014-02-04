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
import android.os.Build;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lightydev.dk.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SQLiteTableEditor {

  private static final String USING_FTS;

  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      USING_FTS = "USING fts4";
    } else {
      USING_FTS = "USING fts3";
    }
  }

  private final SQLiteTable mTable;

  private final List<SQLiteColumn> mColumns = new ArrayList<>();

  private final List<SQLiteUniqueKey> mUniqueKeys = new ArrayList<>();

  private final List<SQLiteIndex> mIndexes = new ArrayList<>();

  private final List<SQLiteTrigger> mTriggers = new ArrayList<>();

  private final List<String> mVirtualTable = new ArrayList<>();

  private int mIndexSequence;

  public SQLiteTableEditor(SQLiteTable table) {
    mTable = table;
  }

  public SQLiteTableEditor addColumn(SQLiteColumn column) {
    mColumns.add(column);
    return this;
  }

  public SQLiteTableEditor addIndex(SQLiteIndex index) {
    mIndexes.add(index.on(mTable.getName(), ++mIndexSequence));
    return this;
  }

  public SQLiteTableEditor addTrigger(SQLiteTrigger trigger) {
    mTriggers.add(trigger);
    return this;
  }

  public SQLiteTableEditor addUniqueKey(SQLiteUniqueKey uniqueKey) {
    mUniqueKeys.add(uniqueKey);
    return this;
  }

  public SQLiteTableEditor usingFts(String... columns) {
    final String ftsTable = mTable.getFtsTableName();
    final List<String> ftsColumns = new ArrayList<>(columns.length);
    for (final String column : columns) {
      final String ftsColumn = SQLite.withFtsSfx(column);
      ftsColumns.add(ftsColumn);
      addColumn(new SQLiteColumn(ftsColumn).text());
    }
    initVirtualTable(ftsTable, ftsColumns);
    initInsertTriggers(ftsTable, ftsColumns);
    initUpdateTriggers(ftsTable, ftsColumns);
    initDeleteTriggers(ftsTable, ftsColumns);
    return this;
  }

  public void create(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE IF NOT EXISTS " + mTable.getName() +
        "(" + TextUtils.join(", ", getFixedDefinition()) + ");");
    if (!mVirtualTable.isEmpty()) {
      db.execSQL(TextUtils.join(" ", mVirtualTable));
    }
    for (final SQLiteIndex index : mIndexes) {
      db.execSQL(index.toString());
    }
    for (final SQLiteTrigger trigger : mTriggers) {
      db.execSQL(trigger.toString());
    }
  }

  public void drop(SQLiteDatabase db) {
    Logger.debug("DROP TABLE IF EXISTS " + mTable.getName() + ";");
  }

  protected List<Object> getFixedDefinition() {
    final List<Object> definition = new ArrayList<>(mColumns.size() + 1 + mUniqueKeys.size());
    boolean hasRowid = false;
    for (final SQLiteColumn column : mColumns) {
      if (column.isRowId()) {
        hasRowid = true;
        column.rowid();
      }
      definition.add(column);
    }
    if (!hasRowid) {
      definition.add(new SQLiteColumn(BaseColumns._ID).rowid());
    }
    definition.addAll(mUniqueKeys);
    return definition;
  }

  private void initVirtualTable(String name, List<String> columns) {
    mVirtualTable.clear();
    mVirtualTable.add("CREATE VIRTUAL TABLE");
    mVirtualTable.add(name);
    mVirtualTable.add(USING_FTS);
    mVirtualTable.add("(");
    mVirtualTable.add(TextUtils.join(", ", columns));
    mVirtualTable.add(");");
  }

  private void initInsertTriggers(String table, List<String> ftsColumns) {
    addTrigger(new SQLiteTrigger(table + "_insert")
        .after().insert().on(mTable.getName())
        .begin()
        .query("DELETE FROM " + table + " WHERE docid=last_insert_rowid();")
        .query("INSERT INTO " + table + "(docid, " + TextUtils.join(", ", ftsColumns) + ")" +
            " VALUES(last_insert_rowid(), NEW." + TextUtils.join(", NEW.", ftsColumns) + ");")
        .end());
  }

  private void initUpdateTriggers(String table, List<String> ftsColumns) {
    for (final String column : ftsColumns) {
      addTrigger(new SQLiteTrigger(table + "_" + column + "_update")
          .updateOf(column).on(mTable.getName())
          .begin()
          .query("UPDATE " + table + " SET " + column + "=NEW." + column + " WHERE docid=OLD." + BaseColumns._ID + ";")
          .end());
    }
  }

  @SuppressWarnings("unused")
  private void initDeleteTriggers(String table, List<String> ftsColumns) {
    addTrigger(new SQLiteTrigger(table + "_delete")
        .after().delete().on(mTable.getName())
        .begin()
        .query("DELETE FROM " + table + " WHERE docid=OLD." + BaseColumns._ID + ";")
        .end());
  }

}
