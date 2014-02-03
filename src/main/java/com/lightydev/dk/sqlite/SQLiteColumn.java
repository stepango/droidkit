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

import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lightydev.dk.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SQLiteColumn {

  private static final String INTEGER = "INTEGER";

  private static final String REAL = "REAL";

  private static final String TEXT = "TEXT";

  private static final String BLOB = "BLOB";

  private static final String NOT_NULL = "NOT NULL";

  private static final String PRIMARY_KEY = "PRIMARY KEY";

  private static final String UNIQUE = "UNIQUE";

  private final List<String> mDefinition = new ArrayList<>(5);

  private final SQLiteConflictClause<SQLiteColumn> mConflictClause = new SQLiteConflictClause<SQLiteColumn>(this) {
    @Override
    protected void onConflict(String clause) {
      mDefinition.set(4, clause);
    }
  };

  public SQLiteColumn(String name) {
    mDefinition.add(name);
    mDefinition.add(TEXT);
    mDefinition.add(null);
    mDefinition.add(null);
    mDefinition.add(null);
  }

  public SQLiteColumn integer() {
    mDefinition.set(1, INTEGER);
    return this;
  }

  public SQLiteColumn real() {
    mDefinition.set(1, REAL);
    return this;
  }

  public SQLiteColumn text() {
    mDefinition.set(1, TEXT);
    return this;
  }

  public SQLiteColumn blob() {
    mDefinition.set(1, BLOB);
    return this;
  }

  public SQLiteColumn notNull() {
    mDefinition.set(2, NOT_NULL);
    return this;
  }

  public SQLiteColumn pk() {
    mDefinition.set(3, PRIMARY_KEY);
    return this;
  }

  public SQLiteColumn rowid() {
    return integer().pk().onConflict().replace();
  }

  public SQLiteColumn unique() {
    mDefinition.set(3, UNIQUE);
    return this;
  }

  public SQLiteConflictClause<SQLiteColumn> onConflict() {
    return mConflictClause;
  }

  @Override
  public String toString() {
    return Strings.joinNonNull(" ", mDefinition);
  }

  boolean isRowId() {
    return TextUtils.equals(mDefinition.get(0), BaseColumns._ID);
  }

}
