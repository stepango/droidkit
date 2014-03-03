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

package com.lightydev.dk.http;

import android.net.Uri;
import android.text.TextUtils;

import com.lightydev.dk.digest.Hash;
import com.lightydev.dk.digest.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class HttpUtils {

  public static final String QUERY_SEPARATOR = "?";

  public static final String PARAMETER_SEPARATOR = "&";

  public static final String KEY_VALUE_SEPARATOR = "=";

  private HttpUtils() {
  }

  public static String getUrlHash(String url) {
    try {
      return Hex.toHexString(Hash.sha1(url.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      return Hex.toHexString(url.getBytes()).substring(0, 40);
    }
  }

  public static String toAsciiUrl(String url) {
    final Uri uri = Uri.parse(url);
    try {
      return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getFragment())
          .toASCIIString();
    } catch (URISyntaxException e) {
      return url;
    }
  }

  public static String toQueryString(Map<String, String> args) {
    final StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : args.entrySet()) {
      if (sb.length() > 0) {
        sb.append(PARAMETER_SEPARATOR);
      }
      sb.append(encode(entry.getKey())).append(KEY_VALUE_SEPARATOR).append(encode(entry.getValue()));
    }
    return sb.toString();
  }

  public static String encode(String value) {
    return encode(value, Charset.defaultCharset().name());
  }

  public static String encode(String value, String charset) {
    try {
      return URLEncoder.encode(value, charset);
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  public static String dumpHeaders(String url, Map<String, String> headers) {
    final StringBuilder dump = new StringBuilder(url).append("\n>>>>>");
    for (final Map.Entry<String, String> header : headers.entrySet()) {
      dump.append('\n').append(header.getKey()).append(": ").append(header.getValue());
    }
    return dump.toString();
  }

  static Map<String, String> readHeaders(HttpURLConnection cn) {
    final Map<String, List<String>> cnHeaders = cn.getHeaderFields();
    if (cnHeaders != null) {
      final Map<String, String> headers = new HashMap<>(cnHeaders.size());
      for (final Map.Entry<String, List<String>> cnHeader : cnHeaders.entrySet()) {
        if (cnHeader.getKey() != null) {
          headers.put(cnHeader.getKey(), TextUtils.join(",", cnHeader.getValue()));
        }
      }
      return Collections.unmodifiableMap(headers);
    }
    return Collections.emptyMap();
  }

  static InputStream readContent(HttpURLConnection cn) {
    try {
      return cn.getInputStream();
    } catch (IOException e) {
      final InputStream error = cn.getErrorStream();
      if (error != null) {
        return error;
      }
      return new ByteArrayInputStream(new byte[0]);
    }
  }

}
