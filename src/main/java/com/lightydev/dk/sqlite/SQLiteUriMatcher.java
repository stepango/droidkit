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

import android.net.Uri;
import android.text.TextUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
class SQLiteUriMatcher {

  public static final int NO_MATCH = -1;

  public static final int MATCH_ALL = 1;

  public static final int MATCH_ID = 2;

  public static final int MATCH_FTS = 3;

  private final Set<String> mAuthorities = new CopyOnWriteArraySet<String>();

  public void addUri(String authority) {
    mAuthorities.add(authority);
  }

  public int match(Uri uri) {
    if (mAuthorities.contains(uri.getAuthority())) {
      final List<String> pathSegments = uri.getPathSegments();
      final int pathSegmentsSize = pathSegments.size();
      if (pathSegmentsSize == 1) {
        return MATCH_ALL;
      } else if (pathSegmentsSize == 2 && TextUtils.isDigitsOnly(pathSegments.get(1))) {
        return MATCH_ID;
      } else if (pathSegmentsSize == 3 && TextUtils.equals(pathSegments.get(1), SQLite.FTS)) {
        return MATCH_FTS;
      }
    }
    return NO_MATCH;
  }

}
