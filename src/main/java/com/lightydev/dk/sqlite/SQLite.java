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

import android.database.DatabaseUtils;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class SQLite {

  public static final String OP_AND = "AND";

  public static final String OP_OR = "OR";

  public static final String SCHEME = "content://";

  static final String FTS = "fts";

  private static final String FTS_SFX = "_" + FTS;

  private SQLite() {
  }

  public static Uri uri(String scheme, String authority, String... segments) {
    final Uri.Builder builder;
    if (scheme.endsWith("://")) {
      builder = Uri.parse(scheme + authority).buildUpon();
    } else {
      builder = Uri.parse(scheme + "://" + authority).buildUpon();
    }
    for (final String segment : segments) {
      builder.appendEncodedPath(segment);
    }
    return builder.build();
  }

  public static Uri fts(String uri, String query) {
    return fts(Uri.parse(uri), query);
  }

  public static Uri fts(Uri uri, String query) {
    if (TextUtils.isEmpty(query)) {
      return uri;
    }
    final StringBuilder fts = new StringBuilder(FTS).append("/");
    DatabaseUtils.appendEscapedSQLString(fts, query + "*");
    return Uri.withAppendedPath(uri, fts.toString());
  }

  public static String joinWhereWith(String operand, String... conditions) {
    return joinWhereWith(operand, Arrays.asList(conditions));
  }

  public static String joinWhereWith(String operand, List<String> conditions) {
    final StringBuilder where = new StringBuilder();
    boolean first = true;
    for (final String condition : conditions) {
      if (!TextUtils.isEmpty(condition)) {
        if (first) {
          first = false;
        } else {
          where.append(' ').append(operand).append(' ');
        }
        where.append(condition);
      }
    }
    return where.toString();
  }

  static Uri baseUri(Uri uri) {
    return uri(uri.getScheme(), uri.getAuthority(), uri.getPathSegments().get(0));
  }

  static String withFtsSfx(String original) {
    return original + FTS_SFX;
  }

}
