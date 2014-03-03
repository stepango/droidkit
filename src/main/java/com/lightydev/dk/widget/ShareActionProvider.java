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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.TypedValue;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.lightydev.dk.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ShareActionProvider extends ActionProvider implements MenuItem.OnMenuItemClickListener {

  private final Context mContext;

  private final Comparator<ResolveInfo> mActivityComparator = new Comparator<ResolveInfo>() {
    @Override
    public int compare(ResolveInfo lhs, ResolveInfo rhs) {
      return onActivityCompare(lhs, rhs);
    }
  };

  private final List<ResolveInfo> mActivities = new ArrayList<>();

  private final AtomicBoolean mReloadActivities = new AtomicBoolean();

  private int mExpandedActivityCount = 3;

  private int mMaxActivityCount = Integer.MAX_VALUE;

  private Intent mShareIntent;

  private int mShareMenuOverflowIcon;

  private int mShareMenuOverflowText;

  public ShareActionProvider(Context context) {
    super(context);
    mContext = context;
    init();
  }

  public void setShareIntent(Intent intent) {
    mShareIntent = intent;
    mReloadActivities.compareAndSet(false, true);
  }

  @Override
  @SuppressWarnings("deprecation")
  public View onCreateActionView() {
    return null;
  }

  @Override
  public void onPrepareSubMenu(SubMenu subMenu) {
    subMenu.clear();
    final List<ResolveInfo> activityList = queryIntentActivities();
    final PackageManager pm = mContext.getPackageManager();
    final int expandedActivityCount = Math.min(mExpandedActivityCount, activityList.size());
    onPrepareSubMenu(subMenu, pm, activityList, expandedActivityCount);
    final int maxActivityCount = Math.min(mMaxActivityCount, activityList.size());
    if (maxActivityCount > expandedActivityCount) {
      final SubMenu collapsedMenu = subMenu.addSubMenu(
          Menu.NONE, expandedActivityCount,
          expandedActivityCount, mShareMenuOverflowText
      ).setIcon(mShareMenuOverflowIcon);
      onPrepareSubMenu(collapsedMenu, pm, activityList, maxActivityCount);
    }
  }

  @Override
  public boolean hasSubMenu() {
    return true;
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }

  protected void onPrepareSubMenu(SubMenu subMenu, PackageManager pm, List<ResolveInfo> activityList, int count) {
    for (int i = 0; i < count; ++i) {
      final ResolveInfo activity = activityList.get(i);
      final Intent intent = resolveShareIntent(pm, activity);
      if (intent != null) {
        subMenu.add(Menu.NONE, i, i, activity.loadLabel(pm))
            .setIcon(activity.loadIcon(pm))
            .setIntent(intent)
            .setOnMenuItemClickListener(this);
      } else {
        mReloadActivities.compareAndSet(false, true);
      }
    }
  }

  protected boolean onActivityAccept(ResolveInfo resolveInfo) {
    return true;
  }

  protected int onActivityCompare(ResolveInfo lhs, ResolveInfo rhs) {
    return 0;
  }

  private void init() {
    initMaxCount();
    initExpandedCount();
    initOverflowMenuIcon();
    initOverflowMenuText();
  }

  private void initMaxCount() {
    final TypedValue outValue = new TypedValue();
    mContext.getTheme().resolveAttribute(R.attr.shareMenuMaxCount, outValue, false);
    if (outValue.data > 0) {
      mMaxActivityCount = outValue.data;
    }
  }

  private void initExpandedCount() {
    final TypedValue outValue = new TypedValue();
    mContext.getTheme().resolveAttribute(R.attr.shareMenuExpandedCount, outValue, false);
    if (outValue.data > 0) {
      mExpandedActivityCount = outValue.data;
    }
  }

  private void initOverflowMenuIcon() {
    final TypedValue outValue = new TypedValue();
    mContext.getTheme().resolveAttribute(R.attr.shareMenuOverflowIcon, outValue, true);
    if (outValue.resourceId == 0) {
      outValue.resourceId = android.R.drawable.ic_menu_share;
    }
    mShareMenuOverflowIcon = outValue.resourceId;
  }

  private void initOverflowMenuText() {
    final TypedValue outValue = new TypedValue();
    mContext.getTheme().resolveAttribute(R.attr.shareMenuOverflowText, outValue, true);
    if (outValue.resourceId == 0) {
      outValue.resourceId = R.string.dk__share_menu_overflow;
    }
    mShareMenuOverflowText = outValue.resourceId;
  }

  private Intent resolveShareIntent(PackageManager pm, ResolveInfo activity) {
    final Intent shareIntent = new Intent(mShareIntent);
    shareIntent.setComponent(new ComponentName(
        activity.activityInfo.packageName,
        activity.activityInfo.name
    ));
    if (pm.resolveActivity(shareIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
      shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      return shareIntent;
    }
    return null;
  }

  private List<ResolveInfo> queryIntentActivities() {
    if (mReloadActivities.compareAndSet(true, false) && mShareIntent != null) {
      mActivities.clear();
      final List<ResolveInfo> activities = mContext.getPackageManager()
          .queryIntentActivities(mShareIntent, PackageManager.MATCH_DEFAULT_ONLY);
      final Iterator<ResolveInfo> activityIterator = activities.iterator();
      while (activityIterator.hasNext()) {
        if (!onActivityAccept(activityIterator.next())) {
          activityIterator.remove();
        }
      }
      Collections.sort(activities, mActivityComparator);
      mActivities.addAll(activities);
    }
    return mActivities;
  }

}
