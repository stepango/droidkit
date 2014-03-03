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

package com.lightydev.dk.widget;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public abstract class PagerCursorAdapter extends PagerAdapter {

  private final DataSetObserver mDataSetObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      onDataSetChanged();
    }
  };

  private final Context mContext;

  private Cursor mCursor;

  protected PagerCursorAdapter(Context context) {
    this(context, null);
  }

  protected PagerCursorAdapter(Context context, Cursor cursor) {
    mContext = context;
    mCursor = cursor;
  }

  public Cursor swapCursor(Cursor newCursor) {
    if (mCursor == newCursor) {
      return null;
    }
    final Cursor oldCursor = mCursor;
    if (oldCursor != null) {
      oldCursor.unregisterDataSetObserver(mDataSetObserver);
    }
    mCursor = newCursor;
    if (mCursor != null) {
      mCursor.registerDataSetObserver(mDataSetObserver);
    }
    notifyDataSetChanged();
    return oldCursor;
  }

  @Override
  public int getCount() {
    if (mCursor != null) {
      return mCursor.getCount();
    }
    return 0;
  }

  @Override
  public boolean isViewFromObject(View view, Object o) {
    return view.equals(o);
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    if (mCursor.moveToPosition(position)) {
      final View view = getItem(mContext, mCursor, container);
      container.addView(view);
      return view;
    }
    throw new IllegalStateException("couldn't move cursor to position " + position);
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  protected void onDataSetChanged() {
    notifyDataSetChanged();
  }

  protected abstract View getItem(Context context, Cursor cursor, ViewGroup parent);

}
