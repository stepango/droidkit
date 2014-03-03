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

import android.content.Intent;
import android.net.Uri;

/**
 * @author Daniel Serdyukov
 * @since 2.2.1
 */
public final class GooglePlayIntents {

  public static final String CATEGORY_APPS = "apps";

  public static final String CATEGORY_MOVIES = "movies";

  public static final String CATEGORY_MUSIC = "music";

  public static final String CATEGORY_BOOKS = "books";

  private GooglePlayIntents() {
  }

  public static Intent forPackage(String packageName) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
  }

  public static Intent forPublisher(String publisher) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + publisher));
  }

  public static Intent search(String query) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + query));
  }

  public static Intent searchApps(String query) {
    return search(query, CATEGORY_APPS);
  }

  public static Intent searchMovies(String query) {
    return search(query, CATEGORY_MOVIES);
  }

  public static Intent searchMusic(String query) {
    return search(query, CATEGORY_MUSIC);
  }

  public static Intent searchBooks(String query) {
    return search(query, CATEGORY_BOOKS);
  }

  private static Intent search(String query, String category) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + query + "&c=" + category));
  }

}
