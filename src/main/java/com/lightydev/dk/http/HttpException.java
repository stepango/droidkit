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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLException;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class HttpException extends IOException {

  public static final int UNKNOWN = 0;

  public static final int NETWORK = 1;

  public static final int SERVER = 2;

  public static final int CLIENT = 3;

  public static final int CACHE = 4;

  public static final int IO = 5;

  private static final List<Class<? extends Throwable>> NETWORK_ERRORS = new CopyOnWriteArrayList<>();

  static {
    NETWORK_ERRORS.add(InterruptedIOException.class);
    NETWORK_ERRORS.add(UnknownHostException.class);
    NETWORK_ERRORS.add(ConnectException.class);
    NETWORK_ERRORS.add(SSLException.class);
  }

  private static final long serialVersionUID = -8879171913418571161L;

  private final int mType;

  public HttpException(int type) {
    super();
    mType = type;
  }

  public HttpException(int type, Throwable cause) {
    super(cause);
    mType = type;
  }

  public HttpException(int type, String message) {
    super(message);
    mType = type;
  }

  public HttpException(int type, String message, Throwable cause) {
    super(message, cause);
    mType = type;
  }

  public int getType() {
    return mType;
  }

  public boolean isNetworkError() {
    return NETWORK_ERRORS.contains(getCause().getClass());
  }

}
