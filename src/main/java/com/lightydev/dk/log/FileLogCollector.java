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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Serdyukov
 */
public class FileLogCollector {

  private static final ThreadLocal<DateFormat> FILE_NAME_FORMAT = new ThreadLocal<DateFormat>() {
    @Override
    protected DateFormat initialValue() {
      return new SimpleDateFormat("dd.MM.yyyy_HH:mm:ss'.log'");
    }
  };

  private final File mLogsDir;

  private final DateFormat mFileNameFormat;

  public FileLogCollector(File logsDir) {
    this(logsDir, FILE_NAME_FORMAT.get());
  }

  public FileLogCollector(File logsDir, DateFormat fileNameFormat) {
    mLogsDir = logsDir;
    mFileNameFormat = fileNameFormat;
  }

  public void collect() throws IOException {
    if (mLogsDir.exists() || mLogsDir.mkdirs()) {
      final File logFile = newLogFile();
      Runtime.getRuntime().exec("logcat -d -v tag -f " + logFile.getAbsolutePath());
      onLogFileSaved(logFile);
    }
  }

  public File getLogsDir() {
    return mLogsDir;
  }

  public DateFormat getFileNameFormat() {
    return mFileNameFormat;
  }

  protected File newLogFile() {
    return new File(getLogsDir(), getFileNameFormat().format(new Date()));
  }

  protected void onLogFileSaved(File logFile) {

  }

}
