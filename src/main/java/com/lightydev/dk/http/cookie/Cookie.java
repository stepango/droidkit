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

import android.text.TextUtils;

import com.lightydev.dk.util.Reflect;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class Cookie implements Comparable<Cookie> {

  private final HttpCookie mNativeCookie;

  private long mLastModified;

  private Cookie(HttpCookie nativeCookie) {
    mNativeCookie = nativeCookie;
  }

  public Cookie(String name, String value) {
    mNativeCookie = new HttpCookie(name, value);
  }

  public static List<Cookie> parse(String header) {
    if (TextUtils.isEmpty(header)) {
      return Collections.emptyList();
    }
    final List<HttpCookie> nativeCookies = HttpCookie.parse(header);
    final List<Cookie> cookies = new ArrayList<>(nativeCookies.size());
    for (final HttpCookie nativeCookie : nativeCookies) {
      cookies.add(new Cookie(nativeCookie));
    }
    return cookies;
  }

  public static List<Cookie> parse(List<String> headers) {
    if (headers == null) {
      return Collections.emptyList();
    }
    final List<Cookie> cookies = new ArrayList<>();
    for (final String header : headers) {
      cookies.addAll(parse(header));
    }
    return cookies;
  }

  public static boolean domainMatches(String cookieDomain, String uriHost) {
    return HttpCookie.domainMatches(cookieDomain, uriHost);
  }

  @Override
  public int compareTo(Cookie another) {
    return 0;
  }

  public void setLastModified(long lastModified) {
    mLastModified = lastModified;
  }

  public boolean isExpired(long time) {
    return mNativeCookie.hasExpired() && (mLastModified + getMaxAge()) < time;
  }

  public long getLastModified() {
    return mLastModified;
  }

  public void setMaxAge(long maxAge) {
    mNativeCookie.setMaxAge(maxAge);
  }

  public long getMaxAge() {
    return mNativeCookie.getMaxAge();
  }

  public String getPath() {
    return mNativeCookie.getPath();
  }

  public String getDomain() {
    return mNativeCookie.getDomain();
  }

  public String getName() {
    return mNativeCookie.getName();
  }

  public String getValue() {
    return mNativeCookie.getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!Reflect.classEquals(this, o)) {
      return false;
    }
    final Cookie another = (Cookie) o;
    return !(mNativeCookie != null ? !mNativeCookie.equals(another.mNativeCookie) : another.mNativeCookie != null);
  }

  @Override
  public int hashCode() {
    return mNativeCookie != null ? mNativeCookie.hashCode() : 0;
  }

  @Override
  public String toString() {
    return getName() + "=" + getValue();
  }

}
