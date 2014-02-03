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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
class SQLiteHelper extends SQLiteOpenHelper {

  private final SQLiteSchema mSchema;

  public SQLiteHelper(Context context, SQLiteSchema schema) {
    super(context, schema.getName(), null, schema.getVersion());
    mSchema = schema;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    mSchema.onCreate(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    mSchema.onUpgrade(db, oldVersion, newVersion);
  }

}
