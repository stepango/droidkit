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

package com.lightydev.dk.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author =Troy= <Daniel Serdyukov>
 */
public final class Hash {

  private static final String MD5 = "MD5";

  private static final String SHA1 = "SHA-1";

  private static final String SHA256 = "SHA-256";

  private Hash() {

  }

  public static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
    return hash(data, MD5);
  }

  public static byte[] sha1(byte[] data) throws NoSuchAlgorithmException {
    return hash(data, SHA1);
  }

  public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
    return hash(data, SHA256);
  }

  public static byte[] hash(byte[] data, String algorithm) throws NoSuchAlgorithmException {
    final MessageDigest hash = MessageDigest.getInstance(algorithm);
    hash.update(data);
    return hash.digest();
  }

}
