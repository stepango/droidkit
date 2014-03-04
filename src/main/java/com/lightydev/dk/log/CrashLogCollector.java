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

import android.os.Build;

import com.lightydev.dk.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Daniel Serdyukov
 */
public class CrashLogCollector extends FileLogCollector implements Thread.UncaughtExceptionHandler {

  private static final String HYPHENS = "--------- ";

  private static final String NEW_LINE = "\n";

  private static final String BEGIN_CRASH_LOG = "beginning of crash log";

  private final Thread.UncaughtExceptionHandler mNativeHandler = Thread.getDefaultUncaughtExceptionHandler();

  private final String mPackageName;

  private final String mVersionName;

  public CrashLogCollector(File logsDir, String packageName, String versionName) {
    super(logsDir);
    mPackageName = packageName;
    mVersionName = versionName;
  }

  private static void printStackTrace(Appendable appendable, Throwable ex) throws IOException {
    appendable.append(ex.toString());
    appendable.append(NEW_LINE);
    for (final StackTraceElement ste : ex.getStackTrace()) {
      appendable.append(ste.toString());
      appendable.append(NEW_LINE);
    }
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    try {
      final File logFile = newLogFile();
      final PrintWriter logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
      try {
        logWriter.write(HYPHENS);
        logWriter.write(mPackageName + " " + mVersionName);
        logWriter.write(NEW_LINE);
        logWriter.write(Build.MANUFACTURER + " " + Build.MODEL + " [" + Build.VERSION.RELEASE + "]");
        logWriter.write(NEW_LINE);
        logWriter.write(HYPHENS + BEGIN_CRASH_LOG);
        logWriter.write(NEW_LINE);
        if (ex.getCause() != null) {
          printStackTrace(logWriter, ex.getCause());
        } else {
          printStackTrace(logWriter, ex);
        }
        onLogFileSaved(logFile);
      } finally {
        IOUtils.closeQuietly(logWriter);
      }
    } catch (IOException e) {
      Logger.error(e);
    }
    mNativeHandler.uncaughtException(thread, ex);
  }

}
