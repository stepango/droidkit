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

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public interface CookiePolicy {

  CookiePolicy ACCEPT_ALL = new CookiePolicy() {
    public boolean shouldAccept(Uri uri, Cookie cookie) {
      return true;
    }
  };

  CookiePolicy ACCEPT_NONE = new CookiePolicy() {
    public boolean shouldAccept(Uri uri, Cookie cookie) {
      return false;
    }
  };

  CookiePolicy ACCEPT_ORIGINAL_SERVER = new CookiePolicy() {
    public boolean shouldAccept(Uri uri, Cookie cookie) {
      return Cookie.domainMatches(cookie.getDomain(), uri.getHost());
    }
  };

  boolean shouldAccept(Uri uri, Cookie cookie);

}
