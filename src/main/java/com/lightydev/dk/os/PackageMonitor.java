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

package com.lightydev.dk.os;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class PackageMonitor extends BroadcastReceiver {

  private static final IntentFilter PACKAGE_FILT = new IntentFilter();

  private static final IntentFilter EXTERNAL_FILT = new IntentFilter();

  static {
    PACKAGE_FILT.addAction(Intent.ACTION_PACKAGE_ADDED);
    PACKAGE_FILT.addAction(Intent.ACTION_PACKAGE_REMOVED);
    PACKAGE_FILT.addAction(Intent.ACTION_PACKAGE_CHANGED);
    EXTERNAL_FILT.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
    EXTERNAL_FILT.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
  }

  private Context mRegisteredContext;

  private Handler mRegisteredHandler;

  static String getPackageName(Intent intent) {
    final Uri uri = intent.getData();
    if (uri != null) {
      return uri.getSchemeSpecificPart();
    }
    return null;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    onBeginPackageChanges();
    boolean somePackagesChanged = false;
    final String action = intent.getAction();
    switch (action) {
      case Intent.ACTION_PACKAGE_ADDED:
        somePackagesChanged = onPackageAddedInternal(intent);
        break;
      case Intent.ACTION_PACKAGE_REMOVED:
        somePackagesChanged = onPackageRemovedInternal(intent);
        break;
      case Intent.ACTION_PACKAGE_CHANGED:
        somePackagesChanged = onPackageChangedInternal(intent);
        break;
      case Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE:
        onExternalApplicationAvailable();
        somePackagesChanged = true;
        break;
      case Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE:
        onExternalApplicationUnavailable();
        somePackagesChanged = true;
        break;
      default:
        break;
    }
    if (somePackagesChanged) {
      onSomePackagesChanged();
    }
    onFinishPackageChanges();
  }

  public void register(Context context, Looper looper, boolean externalStorage) {
    if (mRegisteredContext != null) {
      throw new IllegalStateException("Already registered");
    }
    mRegisteredContext = context;
    if (looper == null) {
      mRegisteredHandler = BackgroundThread.getHandler();
    } else {
      mRegisteredHandler = new Handler(looper);
    }
    context.registerReceiver(this, PACKAGE_FILT, null, mRegisteredHandler);
    if (externalStorage) {
      context.registerReceiver(this, EXTERNAL_FILT, null, mRegisteredHandler);
    }
  }

  public void unregister() {
    if (mRegisteredContext == null) {
      throw new IllegalStateException("Not registered");
    }
    mRegisteredContext.unregisterReceiver(this);
    mRegisteredContext = null;
  }

  protected void onBeginPackageChanges() {
  }

  public void onPackageAdded(String packageName, int uid) {
  }

  public void onPackageModified(String packageName, int uid) {
  }

  public void onPackageRemoved(String packageName, int uid) {
  }

  protected void onExternalApplicationAvailable() {
  }

  protected void onExternalApplicationUnavailable() {
  }

  protected void onSomePackagesChanged() {
  }

  protected void onFinishPackageChanges() {
  }

  private boolean onPackageAddedInternal(Intent intent) {
    final String pkg = getPackageName(intent);
    if (!TextUtils.isEmpty(pkg)) {
      if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
        onPackageModified(pkg, intent.getIntExtra(Intent.EXTRA_UID, 0));
      } else {
        onPackageAdded(pkg, intent.getIntExtra(Intent.EXTRA_UID, 0));
      }
      return true;
    }
    return false;
  }

  private boolean onPackageRemovedInternal(Intent intent) {
    final String pkg = getPackageName(intent);
    if (!TextUtils.isEmpty(pkg) && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
      onPackageRemoved(pkg, intent.getIntExtra(Intent.EXTRA_UID, 0));
      return true;
    }
    return false;
  }

  private boolean onPackageChangedInternal(Intent intent) {
    final String pkg = getPackageName(intent);
    if (!TextUtils.isEmpty(pkg)) {
      onPackageModified(pkg, intent.getIntExtra(Intent.EXTRA_UID, 0));
      return true;
    }
    return false;
  }

}
