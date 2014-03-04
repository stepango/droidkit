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

package com.lightydev.dk.app;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Daniel Serdyukov
 * @since 2.2.1
 */
public final class CommonIntents {

  private CommonIntents() {
  }

  public static Intent openLink(String url) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
  }

  public static Intent webSearch(String query) {
    final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
    intent.putExtra(SearchManager.QUERY, query);
    return intent;
  }

  public static Intent sendEmail(String[] to, String subject, String body) {
    final Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
    intent.putExtra(Intent.EXTRA_EMAIL, to);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(Intent.EXTRA_TEXT, body);
    return intent;
  }

  public static Intent sendSms(String to, String message) {
    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + to));
    intent.putExtra("sms_body", message);
    return intent;
  }

  public static Intent share(String text, String mimeType, Uri... attachments) {
    final Intent intent = new Intent();
    intent.setType(mimeType);
    if (attachments.length > 1) {
      intent.setAction(Intent.ACTION_SEND_MULTIPLE);
      final ArrayList<CharSequence> textExtra = new ArrayList<>();
      textExtra.add(text);
      intent.putCharSequenceArrayListExtra(Intent.EXTRA_TEXT, textExtra);
      final ArrayList<Parcelable> uris = new ArrayList<>();
      Collections.addAll(uris, attachments);
      intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    } else {
      intent.setAction(Intent.ACTION_SEND);
      intent.putExtra(Intent.EXTRA_TEXT, text);
      intent.putExtra(Intent.EXTRA_STREAM, attachments[0]);
    }
    return intent;
  }

  public static Intent shareText(String text) {
    return share(text, HTTP.PLAIN_TEXT_TYPE);
  }

  public static Intent shareImage(String text, Uri... attachments) {
    return share(text, "image/*", attachments);
  }

  public static Intent shareVideo(String text, Uri... attachments) {
    return share(text, "video/*", attachments);
  }

  public static Intent openFile(File file, String mimeType) {
    return openFile(Uri.fromFile(file), mimeType);
  }

  public static Intent openFile(Uri uri, String mimeType) {
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    intent.setType(mimeType);
    return intent;
  }

  public static Intent pickFile(String mimeType) {
    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType(mimeType);
    return intent;
  }

  public static Intent pickImage() {
    final Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
    return intent;
  }

  public static Intent pickContact(String with) {
    final Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://com.android.contacts/contacts"));
    if (!TextUtils.isEmpty(with)) {
      intent.setType(with);
    }
    return intent;
  }

  public static Intent openDialer(String number) {
    return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
  }

  public static Intent capturePhoto(Uri output) {
    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (output != null) {
      intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
    }
    return intent;
  }

  public static Intent captureVideo(Uri output) {
    final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    if (output != null) {
      intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
    }
    return intent;
  }

}
