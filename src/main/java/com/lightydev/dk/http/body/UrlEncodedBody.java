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

package com.lightydev.dk.http.body;

import com.lightydev.dk.http.Http;
import com.lightydev.dk.http.HttpUtils;
import com.lightydev.dk.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public class UrlEncodedBody implements HttpBody {

  public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

  private final Charset mCharset;

  private final Map<String, String> mFormData = new TreeMap<>();

  public UrlEncodedBody() {
    this(Charset.defaultCharset());
  }

  public UrlEncodedBody(Charset charset) {
    mCharset = charset;
  }

  public UrlEncodedBody addEntry(String key, String value) {
    mFormData.put(key, value);
    return this;
  }

  public UrlEncodedBody addEntries(Map<String, String> entries) {
    mFormData.putAll(entries);
    return this;
  }

  @Override
  public String toString() {
    return HttpUtils.toQueryString(mFormData);
  }

  @Override
  public void writeTo(HttpURLConnection cn) throws IOException {
    final byte[] data = toString().getBytes(mCharset);
    cn.setDoOutput(true);
    cn.setFixedLengthStreamingMode(data.length);
    cn.setRequestProperty(Http.Header.CONTENT_TYPE, getContentType());
    final OutputStream out = cn.getOutputStream();
    try {
      out.write(data);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  protected String getContentType() {
    return X_WWW_FORM_URLENCODED;
  }

}
