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

import com.lightydev.dk.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SQLiteTrigger {

  private static final String BEFORE = "BEFORE";

  private static final String AFTER = "AFTER";

  private static final String INSTEAD_OF = "INSTEAD OF";

  private static final String INSERT = "INSERT";

  private static final String UPDATE = "UPDATE";

  private static final String UPDATE_OF = "UPDATE OF";

  private static final String DELETE = "DELETE";

  private static final String BEGIN = "BEGIN";

  private static final String END = "END;";

  private final List<String> mDefinition = new ArrayList<>();

  public SQLiteTrigger(String name) {
    mDefinition.add("CREATE TRIGGER");
    mDefinition.add(name);
    mDefinition.add(null);
    mDefinition.add(null);
    mDefinition.add("ON");
    mDefinition.add(null);
  }

  public SQLiteTrigger before() {
    mDefinition.set(2, BEFORE);
    return this;
  }

  public SQLiteTrigger after() {
    mDefinition.set(2, AFTER);
    return this;
  }

  public SQLiteTrigger insteadOf() {
    mDefinition.set(2, INSTEAD_OF);
    return this;
  }

  public SQLiteTrigger insert() {
    mDefinition.set(3, INSERT);
    return this;
  }

  public SQLiteTrigger update() {
    mDefinition.set(3, UPDATE);
    return this;
  }

  public SQLiteTrigger updateOf(String column) {
    mDefinition.set(3, UPDATE_OF + " " + column);
    return this;
  }

  public SQLiteTrigger delete() {
    mDefinition.set(3, DELETE);
    return this;
  }

  public SQLiteTrigger of() {
    mDefinition.set(4, DELETE);
    return this;
  }

  public SQLiteTrigger on(String table) {
    mDefinition.set(5, table);
    return this;
  }

  public SQLiteTrigger begin() {
    mDefinition.add(BEGIN);
    return this;
  }

  public SQLiteTrigger query(String query) {
    mDefinition.add(query);
    return this;
  }

  public SQLiteTrigger end() {
    mDefinition.add(END);
    return this;
  }

  @Override
  public String toString() {
    return Strings.joinNonNull(" ", mDefinition);
  }

}
