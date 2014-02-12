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

import com.lightydev.dk.http.HttpDate;
import com.lightydev.dk.http.HttpUtils;
import com.lightydev.dk.log.Logger;
import com.lightydev.dk.util.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class DiskCacheStore implements CacheStore {

  private static final String UNIX_HIDDEN = ".";

  // TODO: add 'Cache-Control: max-age' support later
  //private static final Pattern MAX_AGE = Pattern.compile("max\\-age=([\\d]+)");

  private final File mCacheDir;

  private final CachePolicy mCachePolicy;

  private final boolean mDebugMode;

  public DiskCacheStore(File cacheDir) {
    this(cacheDir, CachePolicy.DEFAULT);
  }

  public DiskCacheStore(File cacheDir, boolean debugMode) {
    this(cacheDir, CachePolicy.DEFAULT, debugMode);
  }

  public DiskCacheStore(File cacheDir, CachePolicy policy) {
    this(cacheDir, policy, false);
  }

  public DiskCacheStore(File cacheDir, CachePolicy policy, boolean debugMode) {
    mCacheDir = cacheDir;
    mCachePolicy = policy;
    mDebugMode = debugMode;
    if (!mCacheDir.exists()) {
      mCacheDir.mkdirs();
    }
  }

  private static String getFileName(String url) {
    return HttpUtils.getUrlHash(url);
  }

  @Override
  public CachePolicy getPolicy() {
    return mCachePolicy;
  }

  @Override
  public boolean contains(String url) {
    return new File(mCacheDir, getFileName(url)).exists();
  }

  @Override
  public Entry get(String url) {
    final String fileName = getFileName(url);
    final File cacheFile = new File(mCacheDir, fileName);
    final File metaFile = new File(mCacheDir, UNIX_HIDDEN + fileName);
    if (cacheFile.exists() && metaFile.exists()) {
      try {
        final DataInputStream meta = new DataInputStream(new FileInputStream(metaFile));
        try {
          return new Entry(cacheFile, meta.readUTF(), meta.readLong(), meta.readLong());
        } finally {
          IOUtils.closeQuietly(meta);
        }
      } catch (IOException e) {
        if (mDebugMode) {
          Logger.error(e);
        }
        removeCacheEntry(cacheFile, metaFile);
      }
    }
    return null;
  }

  @Override
  public Entry put(String url, Map<String, String> headers, InputStream content) {
    final String fileName = getFileName(url);
    final File cacheFile = new File(mCacheDir, fileName);
    final File metaFile = new File(mCacheDir, UNIX_HIDDEN + fileName);
    try {
      saveMetaFile(metaFile, headers);
      saveCacheFile(cacheFile, content);
    } catch (IOException e) {
      if (mDebugMode) {
        Logger.error(e);
      }
      removeCacheEntry(cacheFile, metaFile);
    }
    return get(url);
  }

  @Override
  public Entry update(String url, Map<String, String> headers) {
    final String fileName = getFileName(url);
    final File cacheFile = new File(mCacheDir, fileName);
    final File metaFile = new File(mCacheDir, UNIX_HIDDEN + fileName);
    if (cacheFile.exists() && metaFile.exists()) {
      try {
        saveMetaFile(metaFile, headers);
      } catch (IOException e) {
        if (mDebugMode) {
          Logger.error(e);
        }
        removeCacheEntry(cacheFile, metaFile);
      }
    }
    return get(url);
  }

  @Override
  public boolean clear() {
    for (final File file : mCacheDir.listFiles()) {
      file.delete();
    }
    return mCacheDir.delete();
  }

  private void saveMetaFile(File metaFile, Map<String, String> headers) throws IOException {
    final DataOutputStream meta = new DataOutputStream(new FileOutputStream(metaFile));
    try {
      writeUTF(meta, headers.get(CachePolicy.Header.ETAG));
      writeLong(meta, headers.get(CachePolicy.Header.LAST_MODIFIED));
      writeLong(meta, headers.get(CachePolicy.Header.EXPIRES));
    } finally {
      IOUtils.closeQuietly(meta);
    }
  }

  private void writeUTF(DataOutputStream meta, String etag) throws IOException {
    if (TextUtils.isEmpty(etag)) {
      meta.writeUTF("");
    } else {
      meta.writeUTF(etag);
    }
  }

  private void writeLong(DataOutputStream meta, String lastModified) throws IOException {
    if (TextUtils.isEmpty(lastModified)) {
      meta.writeLong(0);
    } else {
      meta.writeLong(HttpDate.parse(lastModified).getTime());
    }
  }

  private void saveCacheFile(File cacheFile, InputStream content) throws IOException {
    final FileOutputStream cache = new FileOutputStream(cacheFile);
    try {
      IOUtils.copy(content, cache);
    } finally {
      IOUtils.closeQuietly(cache);
    }
  }

  private void removeCacheEntry(File cacheFile, File metaFile) {
    if (metaFile.exists()) {
      metaFile.delete();
    }
    if (cacheFile.exists()) {
      cacheFile.delete();
    }
  }

  public static class Entry implements CacheStore.Entry {

    private final File mCacheFile;

    private final String mETag;

    private final long mLastModified;

    private final long mExpires;

    public Entry(File cacheFile, String etag, long lastModified, long expires) {
      mCacheFile = cacheFile;
      mETag = etag;
      mLastModified = lastModified;
      mExpires = expires;
    }

    @Override
    public boolean isExpired() {
      return mExpires < System.currentTimeMillis();
    }

    @Override
    public InputStream getContent() throws IOException {
      return new FileInputStream(mCacheFile);
    }

    @Override
    public String getETag() {
      return mETag;
    }

    @Override
    public long getLastModified() {
      return mLastModified;
    }

    @Override
    public String toString() {
      return "Entry{" +
          "file=" + mCacheFile.getName() +
          ", etag='" + mETag + '\'' +
          ", last-modified=" + HttpDate.format(mLastModified) +
          ", expired=" + isExpired() +
          '}';
    }

  }

}
