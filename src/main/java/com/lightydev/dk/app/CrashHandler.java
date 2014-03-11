package com.lightydev.dk.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Inject some info about android version and the device, since google can't provide them in the developer console
 *
 * Example of usage:
 * CrashHandler.init(context, CrashHandler.BUILD_DATE | CrashHandler.FINGERPRINT);
 *
 * @author Stepan Goncharov
 * @since 2.3.1
 */
public final class CrashHandler implements UncaughtExceptionHandler {

    public static final int MODEL = 1;
    public static final int VERSION = 1 << 1;
    public static final int FINGERPRINT = 1 << 2;
    public static final int BUILD_DATE = 1 << 3;
    public static final int APP_VERSION = 1 << 4;

    public static final int[] FLAGS_LIST = new int[]{
            MODEL, VERSION, FINGERPRINT, BUILD_DATE, APP_VERSION
    };

    private UncaughtExceptionHandler mDefaultUEH;
    private Context mContext;

    private final ArrayList<StackTraceElement> mInfo = new ArrayList<>();

    private CrashHandler(final Context applicationContext, final int flags) {
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        mContext = applicationContext;
        generateInfo(flags, mInfo);
    }

    public static synchronized CrashHandler init(final Context context, final int flags) {
        final CrashHandler handler = new CrashHandler(context, flags);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        return handler;
    }

    private void generateInfo(final int flags, final ArrayList<StackTraceElement> info) {
        for (int flag : FLAGS_LIST) {
            if (hasFlag(flags, flag)) {
                info.add(makeStackTraceElement(flag));
            }
        }
    }

    private boolean hasFlag(final int flags, final int target) {
        return (flags & target) > 0;
    }

    private StackTraceElement makeStackTraceElement(final int flag) {
        switch (flag) {
            case MODEL:
                return new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
            case VERSION:
                return new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
            case FINGERPRINT:
                return new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
            case APP_VERSION:
                return new StackTraceElement("App", "VERSION", getAppVersion(), -1);
            case BUILD_DATE:
                return new StackTraceElement("App", "BUILD_DATE", getBuildDate(), -1);
            default:
                return null;
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final StackTraceElement[] trace = ex.getStackTrace();
        final StackTraceElement[] trace2 = new StackTraceElement[trace.length + mInfo.size()];
        System.arraycopy(trace, 0, trace2, 0, trace.length);
        for (int i = trace.length; i < trace2.length; i++) {
            trace2[i] = mInfo.get(i - trace.length);
        }
        ex.setStackTrace(trace2);

        mDefaultUEH.uncaughtException(thread, ex);
    }

    private String getBuildDate() {
        try {
            final ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), 0);
            final ZipFile zf;
            zf = new ZipFile(ai.sourceDir);
            final ZipEntry ze = zf.getEntry("classes.dex");
            final long time = ze.getTime();
            return SimpleDateFormat.getInstance().format(new java.util.Date(time));
        } catch (IOException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
    }

    private String getAppVersion() {
        try {
            return getPackageInfo().versionName + " v" + getPackageInfo().versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

}
