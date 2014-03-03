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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public interface CacheStore {

  CacheStore NO_CACHE = new NoCacheStore();

  CachePolicy getPolicy();

  boolean contains(String url);

  Entry get(String url);

  Entry put(String url, Map<String, String> headers, InputStream content);

  Entry update(String url, Map<String, String> headers);

  boolean clear();

  interface Entry {

    boolean isExpired();

    InputStream getContent() throws IOException;

    String getETag();

    long getLastModified();

  }

}
