/*
 * Copyright 2012-2013 Daniel Serdyukov
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

package com.lightydev.dk.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class Intents {

  public static final String PLAY_STORE_APPS = "apps";

  private static final String HTTP_SCHEMA = "http://";

  public static Intent openLink(String url) {
    if (!TextUtils.isEmpty(url) && !url.contains("://")) {
      url = HTTP_SCHEMA + url;
    }
    return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
  }

  public static Intent openPlayStoreDetails(Context context) {
    return openPlayStoreDetails(context.getPackageName());
  }

  public static Intent openPlayStoreDetails(String packageName) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
  }

  public static Intent openPlayStoreSearch(String query, String category) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + query + "&c=" + category));
  }

  public static Intent openPlayStoreSearchApps(String query) {
    return openPlayStoreSearch(query, PLAY_STORE_APPS);
  }

  public static Intent openPlayStorePublisher(String publisher) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + publisher));
  }

  public static Intent sendEmail(String[] to, String subject, String body) {
    return new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + TextUtils.join(",", to) +
        "?subject=" + subject + "&body=" + body));
  }

  public static Intent shareText(String text, String subject) {
    final Intent intent = new Intent(Intent.ACTION_SEND);
    if (!TextUtils.isEmpty(subject)) {
      intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    }
    intent.putExtra(Intent.EXTRA_TEXT, text);
    intent.setType("text/plain");
    return intent;
  }

  public static Intent sendSms(String to, String message) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + to));
    intent.putExtra("address", to);
    intent.putExtra("sms_body", message);
    intent.setType("vnd.android-dir/mms-sms");
    return intent;
  }

  public static Intent openDialer(String number) {
    return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
  }

  /**
   * @see {@link android.provider.ContactsContract.CommonDataKinds}
   */
  public static Intent pickContact(String with) {
    final Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://com.android.contacts/contacts"));
    if (!TextUtils.isEmpty(with)) {
      intent.setType(with);
    }
    return intent;
  }

  public static Intent pickFile(String type) {
    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType(type);
    return intent;
  }

  public static Intent pickImage() {
    final Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
    return intent;
  }

  public static Intent takePhoto(String file) {
    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (!TextUtils.isEmpty(file)) {
      intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(file)));
    }
    return intent;
  }

  public static Intent openFile(String path, String mimeType) {
    return openFile(Uri.parse(path), mimeType);
  }

  public static Intent openFile(File file, String mimeType) {
    return openFile(Uri.fromFile(file), mimeType);
  }

  public static Intent openFile(Uri uri, String mimeType) {
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    intent.setType(mimeType);
    return intent;
  }

}
