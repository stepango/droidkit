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

import com.lightydev.dk.http.HttpDate;
import com.lightydev.dk.util.Reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class Cookie implements Comparable<Cookie> {

  private static final Set<String> RESERVED_NAMES = new HashSet<>();

  static {
    RESERVED_NAMES.add("comment");    //           RFC 2109  RFC 2965
    RESERVED_NAMES.add("commenturl"); //                     RFC 2965
    RESERVED_NAMES.add("discard");    //                     RFC 2965
    RESERVED_NAMES.add("domain");     // Netscape  RFC 2109  RFC 2965
    RESERVED_NAMES.add("expires");    // Netscape
    RESERVED_NAMES.add("max-age");    //           RFC 2109  RFC 2965
    RESERVED_NAMES.add("path");       // Netscape  RFC 2109  RFC 2965
    RESERVED_NAMES.add("port");       //                     RFC 2965
    RESERVED_NAMES.add("secure");     // Netscape  RFC 2109  RFC 2965
    RESERVED_NAMES.add("version");    //           RFC 2109  RFC 2965
  }

  private static final ThreadLocal<Parser> PARSER = new ThreadLocal<Parser>() {
    @Override
    protected Parser initialValue() {
      return new Parser();
    }
  };

  private final String mName;

  private final String mValue;

  private long mExpires;

  private String mPath;

  private String mDomain;

  public Cookie(String name, String value) {
    name = name.trim();
    if (RESERVED_NAMES.contains(name)) {
      throw new IllegalArgumentException("Wrong cookie name (name reserved by RFC 2965 or RFC 2109)");
    }
    mName = name;
    mValue = value;
  }

  public static List<Cookie> parse(String header) {
    if (TextUtils.isEmpty(header)) {
      return Collections.emptyList();
    }
    return PARSER.get().parse(header);
  }

  public static List<Cookie> parse(List<String> headers) {
    if (headers == null) {
      return Collections.emptyList();
    }
    final List<Cookie> cookies = new ArrayList<>();
    for (final String value : headers) {
      cookies.addAll(parse(value));
    }
    return cookies;
  }

  public static boolean domainMatches(String cookieDomain, String uriHost) {
    if (!TextUtils.isEmpty(cookieDomain) && !TextUtils.isEmpty(uriHost)) {
      cookieDomain = cookieDomain.toLowerCase(Locale.US);
      uriHost = uriHost.toLowerCase(Locale.US);
      return isFullyQualifiedDomainName(cookieDomain) && uriHost.matches("(.*\\.)?" + cookieDomain);
    }
    return false;
  }

  public static boolean pathMatches(String cookiePath, String uriPath) {
    return TextUtils.equals(cookiePath.toLowerCase(), uriPath.toLowerCase());
  }

  public static String pathToCookiePath(String path) {
    if (path == null) {
      return "/";
    }
    return path.substring(0, path.lastIndexOf('/') + 1);
  }

  public static boolean isFullyQualifiedDomainName(String name) {
    if (name.startsWith(".")) {
      name = name.substring(1);
    }
    final int dot = name.indexOf('.');
    return dot != -1 && dot < name.length() - 1;
  }

  public void setExpires(long expires) {
    mExpires = expires;
  }

  public boolean isExpired(long time) {
    return mExpires < time;
  }

  public String getPath() {
    return mPath;
  }

  public void setPath(String path) {
    mPath = path;
  }

  public String getDomain() {
    return mDomain;
  }

  public void setDomain(String domain) {
    mDomain = domain;
  }

  public String getName() {
    return mName;
  }

  public String getValue() {
    return mValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!Reflect.classEquals(this, o)) {
      return false;
    }
    final Cookie cookie = (Cookie) o;
    return TextUtils.equals(mDomain, cookie.mDomain)
        && TextUtils.equals(mName, cookie.mName)
        && TextUtils.equals(mPath, cookie.mPath);
  }

  @Override
  public int hashCode() {
    int result = mDomain != null ? mDomain.hashCode() : 0;
    result = 31 * result + (mName != null ? mName.hashCode() : 0);
    result = 31 * result + (mPath != null ? mPath.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return mName + "=" + mValue;
  }

  @Override
  public int compareTo(Cookie another) {
    return 0;
  }

  private static class Parser {

    private static final Pattern sCookiePattern = Pattern.compile("([^=]+)=([^;]+);?");

    public List<Cookie> parse(String header) {
      final List<Cookie> cookies = new ArrayList<>();
      final Matcher matcher = sCookiePattern.matcher(header);
      Cookie cookie = null;
      while (matcher.find()) {
        final String key = matcher.group(1).trim();
        final String value = matcher.group(2).trim();
        if (cookie == null) {
          cookie = new Cookie(key, value);
        } else {
          setAttribute(cookie, key, value);
        }
      }
      cookies.add(cookie);
      return cookies;
    }

    private void setAttribute(Cookie cookie, String name, String value) {
      switch (name) {
        case "expires":
          cookie.setExpires(HttpDate.parse(value).getTime());
          break;
        case "path":
          cookie.setPath(value);
          break;
        case "domain":
          cookie.setDomain(value);
          break;
      }
    }

  }

}
