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

import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public interface CachePolicy {

  CachePolicy NO_CACHE = new NoCachePolicy();

  CachePolicy DEFAULT = new DefaultCachePolicy();

  boolean ignoreCache(String url);

  boolean shouldCache(String url);

  boolean shouldCache(String url, Map<String, String> headers);

  interface Header {

    String ETAG = "ETag";

    String LAST_MODIFIED = "Last-Modified";

    String EXPIRES = "Expires";

    String CACHE_CONTROL = "Cache-Control";

  }

}
