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

package com.lightydev.dk.http.cookie;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class SQLiteCookieStore implements CookieStore {

  private final ContentResolver mDb;

  private final CookiePolicy mPolicy;

  private final Uri mUri;

  public SQLiteCookieStore(ContentResolver db, Uri uri) {
    this(db, CookiePolicy.ACCEPT_ALL, uri);
  }

  public SQLiteCookieStore(ContentResolver db, CookiePolicy policy, Uri uri) {
    mDb = db;
    mPolicy = policy;
    mUri = uri;
  }

  @Override
  public void add(Uri uri, List<Cookie> cookies) {
    final String host = uri.getHost();
    final long now = System.currentTimeMillis();
    final List<ContentValues> bulkValues = new ArrayList<>(cookies.size());
    for (final Cookie cookie : cookies) {
      if (mPolicy.shouldAccept(uri, cookie)) {
        cookie.setLastModified(now);
        bulkValues.add(SQLiteCookie.toContentValues(host, cookie));
      }
    }
    mDb.bulkInsert(mUri, bulkValues.toArray(new ContentValues[bulkValues.size()]));
  }

  @Override
  public List<Cookie> get(Uri uri) {
    final Cursor c = mDb.query(
        mUri, null, SQLiteCookie.Columns.HOST + "=?",
        new String[]{uri.getHost()}, null
    );
    try {
      if (c.moveToFirst()) {
        return SQLiteCookie.toCookieJar(c);
      }
    } finally {
      c.close();
    }
    return Collections.emptyList();
  }

  @Override
  public boolean remove(Uri uri, Cookie cookie) {
    return mDb.delete(
        mUri, SQLiteCookie.Columns.HOST + "=? AND " + SQLiteCookie.Columns.NAME + "=?",
        new String[]{uri.getHost(), cookie.getName()}
    ) > 0;
  }

  @Override
  public void removeAll(Uri uri) {
    mDb.delete(mUri, SQLiteCookie.Columns.HOST + "=?", new String[]{uri.getHost()});
  }

  @Override
  public void removeAll() {
    mDb.delete(mUri, null, null);
  }

  @Override
  public void removeExpired() {
    mDb.delete(
        mUri, SQLiteCookie.Columns.MAX_AGE + " <> -1" +
        " AND (" + SQLiteCookie.Columns.LAST_MODIFIED + " + " + SQLiteCookie.Columns.MAX_AGE + ") < ?",
        new String[]{String.valueOf(System.currentTimeMillis())}
    );
  }

}
