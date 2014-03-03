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

package com.lightydev.dk.json;

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class Json {

  private Json() {
  }

  public static ContentValues parseObject(JSONObject object, Map<String, String> projection) throws JSONException {
    final ContentValues values = new ContentValues();
    for (final Map.Entry<String, String> entry : projection.entrySet()) {
      if (object.has(entry.getValue())) {
        if (object.isNull(entry.getValue())) {
          values.putNull(entry.getKey());
        } else {
          values.put(entry.getKey(), String.valueOf(object.get(entry.getValue())));
        }
      }
    }
    return values;
  }

  public static ContentValues[] parseArray(JSONArray array, Map<String, String> projection) throws JSONException {
    final int length = array.length();
    final ContentValues[] bulkValues = new ContentValues[length];
    for (int i = 0; i < length; ++i) {
      bulkValues[i] = parseObject(array.getJSONObject(i), projection);
    }
    return bulkValues;
  }

}
