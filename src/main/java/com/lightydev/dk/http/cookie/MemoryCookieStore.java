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

import android.net.Uri;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class MemoryCookieStore implements CookieStore {

  private final Map<String, Set<Cookie>> mCookieStore = new ConcurrentHashMap<>();

  private final CookiePolicy mPolicy;

  public MemoryCookieStore() {
    this(CookiePolicy.ACCEPT_ALL);
  }

  public MemoryCookieStore(CookiePolicy policy) {
    mPolicy = policy;
  }

  @Override
  public void add(Uri uri, List<Cookie> cookies) {
    final Set<Cookie> cookieJar = obtainCookieJar(uri.getHost());
    for (final Cookie cookie : cookies) {
      if (mPolicy.shouldAccept(uri, cookie)) {
        cookieJar.add(cookie);
      }
    }
    removeExpired();
  }

  @Override
  public List<Cookie> get(Uri uri) {
    return new ArrayList<>(obtainCookieJar(uri.getHost()));
  }

  @Override
  public boolean remove(Uri uri, Cookie cookie) {
    return obtainCookieJar(uri.getHost()).remove(cookie);
  }

  @Override
  public void removeAll(Uri uri) {
    mCookieStore.remove(uri.getHost());
  }

  @Override
  public void removeAll() {
    mCookieStore.clear();
  }

  @Override
  public void removeExpired() {
    final long now = System.currentTimeMillis();
    for (final Set<Cookie> cookieJar : mCookieStore.values()) {
      final Iterator<Cookie> cookieIterator = cookieJar.iterator();
      while (cookieIterator.hasNext()) {
        if (cookieIterator.next().isExpired(now)) {
          cookieIterator.remove();
        }
      }
    }
  }

  private Set<Cookie> obtainCookieJar(String host) {
    Set<Cookie> cookieJar = mCookieStore.get(host);
    if (cookieJar == null) {
      cookieJar = new ConcurrentSkipListSet<>();
      mCookieStore.put(host, cookieJar);
    }
    return cookieJar;
  }

}
