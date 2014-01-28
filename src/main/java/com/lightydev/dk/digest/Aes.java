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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class Aes {

  private static final String AES = "AES";

  private static final String SHA1PRNG = "SHA1PRNG";

  private Aes() {
  }

  public static byte[] encrypt(byte[] data, byte[] key) throws AesException {
    return doFinal(Cipher.ENCRYPT_MODE, data, key);
  }

  public static byte[] decrypt(byte[] data, byte[] key) throws AesException {
    return doFinal(Cipher.DECRYPT_MODE, data, key);
  }

  public static byte[] getRawKey(byte[] key) throws AesException {
    try {
      final KeyGenerator keygen = KeyGenerator.getInstance(AES);
      final SecureRandom prng = SecureRandom.getInstance(SHA1PRNG);
      prng.setSeed(key);
      keygen.init(128, prng);
      return keygen.generateKey().getEncoded();
    } catch (NoSuchAlgorithmException e) {
      throw new AesException(e);
    }
  }

  private static byte[] doFinal(int mode, byte[] data, byte[] key) throws AesException {
    try {
      final SecretKeySpec secret = new SecretKeySpec(key, AES);
      final Cipher cipher = Cipher.getInstance(AES);
      cipher.init(mode, secret);
      return cipher.doFinal(data);
    } catch (NoSuchAlgorithmException
        | IllegalBlockSizeException
        | InvalidKeyException
        | BadPaddingException
        | NoSuchPaddingException e) {
      throw new AesException(e);
    }
  }

}
