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

import android.text.TextUtils;

import com.lightydev.dk.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class SQLiteUniqueKey {

  private final List<String> mDefinition = new ArrayList<>(5);

  private final SQLiteConflictClause<SQLiteUniqueKey> mConflictClause =
      new SQLiteConflictClause<SQLiteUniqueKey>(this) {
        @Override
        protected void onConflict(String clause) {
          mDefinition.set(4, clause);
        }
      };

  public SQLiteUniqueKey(String... columns) {
    mDefinition.add("UNIQUE");
    mDefinition.add("(");
    mDefinition.add(TextUtils.join(", ", columns));
    mDefinition.add(")");
    mDefinition.add(null);
  }

  public SQLiteConflictClause<SQLiteUniqueKey> onConflict() {
    return mConflictClause;
  }

  @Override
  public String toString() {
    return Strings.joinNonNull(" ", mDefinition);
  }

}
