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

package com.lightydev.dk.http.callback;

import com.lightydev.dk.http.HttpException;

import java.io.InputStream;
import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public interface AsyncHttpCallback {

  void onSuccess(int statusCode, Map<String, String> headers, InputStream content);

  void onError(HttpException e);

}
