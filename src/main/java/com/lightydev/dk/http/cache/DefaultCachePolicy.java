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

package com.lightydev.dk.http.cache;

import android.text.TextUtils;

import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class DefaultCachePolicy implements CachePolicy {

  private static final String NO_CACHE = "no-cache";

  @Override
  public boolean ignoreCache(String url) {
    return false;
  }

  @Override
  public boolean shouldCache(String url) {
    return true;
  }

  @Override
  public boolean shouldCache(String url, Map<String, String> headers) {
    final String header = headers.get(Header.CACHE_CONTROL);
    return (TextUtils.isEmpty(header) || !header.contains(NO_CACHE)) && shouldCache(url);
  }

}
