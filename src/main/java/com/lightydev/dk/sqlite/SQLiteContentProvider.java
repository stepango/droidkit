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

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.lightydev.dk.DroidKit;

import java.util.ArrayList;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public abstract class SQLiteContentProvider extends ContentProvider {

  private static final String MIME_DIR = "vnd.android.cursor.dir/";

  private static final String MIME_ITEM = "vnd.android.cursor.item/";

  private final SQLiteUriMatcher mUriMatcher = new SQLiteUriMatcher();

  private SQLiteSchema mSchema;

  private SQLiteOpenHelper mHelper;

  @Override
  public boolean onCreate() {
    try {
      final ProviderInfo pi = DroidKit.getProviderInfo(getContext(), getClass(), 0);
      final String[] authorities = TextUtils.split(pi.authority, ";");
      for (final String authority : authorities) {
        mUriMatcher.addUri(authority);
      }
      mSchema = onCreateSchema();
      mHelper = new SQLiteHelper(getContext(), mSchema);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      throw new SQLiteException(e.getMessage());
    }
  }

  @Override
  public Cursor query(Uri uri, String[] columns, String where, String[] whereArgs, String orderBy) {
    switch (mUriMatcher.match(uri)) {
      case SQLiteUriMatcher.MATCH_ALL:
        return withNotificationUri(uri, mSchema.acquireTable(uri)
            .select(mHelper.getReadableDatabase(), columns, where, whereArgs, orderBy));
      case SQLiteUriMatcher.MATCH_ID:
        return withNotificationUri(uri, mSchema.acquireTable(uri).select(
            mHelper.getReadableDatabase(), columns, BaseColumns._ID + "=?",
            new String[]{uri.getLastPathSegment()}, orderBy
        ));
      case SQLiteUriMatcher.MATCH_FTS:
        return withNotificationUri(uri, mSchema.acquireTable(uri)
            .fts(mHelper.getReadableDatabase(), uri.getLastPathSegment(), columns, where, whereArgs, orderBy));
      default:
        throw new SQLiteException("unknown uri " + uri);
    }
  }

  @Override
  public String getType(Uri uri) {
    switch (mUriMatcher.match(uri)) {
      case SQLiteUriMatcher.MATCH_ALL:
      case SQLiteUriMatcher.MATCH_FTS:
        return MIME_DIR + mSchema.acquireTable(uri).getName();
      case SQLiteUriMatcher.MATCH_ID:
        return MIME_ITEM + mSchema.acquireTable(uri).getName();
      default:
        throw new SQLiteException("unknown uri " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    switch (mUriMatcher.match(uri)) {
      case SQLiteUriMatcher.MATCH_ALL:
        return withAppendedId(uri, mSchema.acquireTable(uri)
            .insert(mHelper.getWritableDatabase(), values));
      case SQLiteUriMatcher.MATCH_ID:
        return withAppendedId(uri, mSchema.acquireTable(uri)
            .insertById(mHelper.getWritableDatabase(), values, uri.getLastPathSegment()));
      default:
        throw new SQLiteException("unknown uri " + uri);
    }
  }

  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {
    switch (mUriMatcher.match(uri)) {
      case SQLiteUriMatcher.MATCH_ALL:
        return notifyChangeIfNecessary(uri, mSchema.acquireTable(uri)
            .delete(mHelper.getWritableDatabase(), where, whereArgs));
      case SQLiteUriMatcher.MATCH_ID:
        return notifyChangeIfNecessary(uri, mSchema.acquireTable(uri)
            .deleteById(mHelper.getWritableDatabase(), uri.getLastPathSegment()));
      default:
        throw new SQLiteException("unknown uri " + uri);
    }
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
    switch (mUriMatcher.match(uri)) {
      case SQLiteUriMatcher.MATCH_ALL:
        return notifyChangeIfNecessary(uri, mSchema.acquireTable(uri)
            .update(mHelper.getWritableDatabase(), values, where, whereArgs));
      case SQLiteUriMatcher.MATCH_ID:
        return notifyChangeIfNecessary(uri, mSchema.acquireTable(uri)
            .updateById(mHelper.getWritableDatabase(), values, uri.getLastPathSegment()));
      default:
        throw new SQLiteException("unknown uri " + uri);
    }
  }

  @Override
  public int bulkInsert(Uri uri, ContentValues[] bulkValues) {
    switch (mUriMatcher.match(uri)) {
      case SQLiteUriMatcher.MATCH_ALL:
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        SQLiteDatabaseCompat.beginTransactionNonExclusive(db);
        try {
          final SQLiteTable table = mSchema.acquireTable(uri);
          for (final ContentValues values : bulkValues) {
            table.insert(db, values);
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        return notifyChangeIfNecessary(uri, bulkValues.length);
      case SQLiteUriMatcher.MATCH_FTS:
      case SQLiteUriMatcher.MATCH_ID:
        throw new UnsupportedOperationException();
      default:
        throw new SQLiteException("unknown uri " + uri);
    }
  }

  @Override
  public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
      throws OperationApplicationException {
    final int numOperations = operations.size();
    final ContentProviderResult[] results = new ContentProviderResult[numOperations];
    final SQLiteDatabase db = mHelper.getWritableDatabase();
    SQLiteDatabaseCompat.beginTransactionNonExclusive(db);
    try {
      for (int i = 0; i < numOperations; ++i) {
        results[i] = operations.get(i).apply(this, results, i);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return results;
  }

  protected abstract SQLiteSchema onCreateSchema();

  private Cursor withNotificationUri(Uri uri, Cursor c) {
    c.setNotificationUri(getContext().getContentResolver(), SQLite.baseUri(uri));
    return c;
  }

  private Uri withAppendedId(Uri uri, long id) {
    final Uri baseUri = SQLite.baseUri(uri);
    getContext().getContentResolver().notifyChange(baseUri, null);
    return ContentUris.withAppendedId(baseUri, id);
  }

  private int notifyChangeIfNecessary(Uri uri, int affectedRows) {
    if (affectedRows > 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return affectedRows;
  }

}
