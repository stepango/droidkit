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

package com.lightydev.dk.log;

import android.graphics.Bitmap;
import android.view.View;

import com.lightydev.dk.hardware.ShakeDetector;
import com.lightydev.dk.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Daniel Serdyukov
 */
public class ShakeLogCollector extends FileLogCollector implements ShakeDetector.OnShakeListener {

  private final View mDecorView;

  private OnReadyListener mReadyListener;

  public ShakeLogCollector(File logsDir, View decorView) {
    super(logsDir);
    mDecorView = decorView;
  }

  public void setOnReadyListener(OnReadyListener l) {
    mReadyListener = l;
  }

  @Override
  public void onShake(ShakeDetector detector) {
    try {
      collect();
    } catch (IOException e) {
      Logger.error(e);
    }
  }

  @Override
  protected void onLogFileSaved(File logFile) {
    final boolean drawingCacheEnabled = mDecorView.isDrawingCacheEnabled();
    try {
      final File screenFile = new File(getLogsDir(), getLogFileName() + ".jpg");
      final FileOutputStream screenOut = new FileOutputStream(screenFile);
      try {
        mDecorView.setDrawingCacheEnabled(true);
        mDecorView.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 90, screenOut);
        onShakeLogsReady(logFile, screenFile);
      } finally {
        mDecorView.setDrawingCacheEnabled(drawingCacheEnabled);
        IOUtils.closeQuietly(screenOut);
      }
    } catch (FileNotFoundException e) {
      Logger.error(e);
    }
  }

  protected void onShakeLogsReady(File logFile, File screenshot) {
    if (mReadyListener != null) {
      mReadyListener.onShakeLogsReady(logFile, screenshot);
    }
  }

  public interface OnReadyListener {
    void onShakeLogsReady(File logFile, File screenshot);
  }

}
