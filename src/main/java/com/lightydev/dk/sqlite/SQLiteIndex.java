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
public class SQLiteIndex {

  private static final String UNIQUE = "UNIQUE";

  private final List<String> mDefinition = new ArrayList<>(9);

  public SQLiteIndex(String... columns) {
    mDefinition.add("CREATE");
    mDefinition.add(null);
    mDefinition.add("INDEX IF NOT EXISTS");
    mDefinition.add(null);
    mDefinition.add("ON");
    mDefinition.add(null);
    mDefinition.add("(");
    mDefinition.add(TextUtils.join(", ", columns));
    mDefinition.add(");");
  }

  public SQLiteIndex unique() {
    mDefinition.set(1, UNIQUE);
    return this;
  }

  @Override
  public String toString() {
    return Strings.joinNonNull(" ", mDefinition);
  }

  SQLiteIndex on(String table, int sequence) {
    mDefinition.set(3, table + "_idx_" + sequence);
    mDefinition.set(5, table);
    return this;
  }

}
