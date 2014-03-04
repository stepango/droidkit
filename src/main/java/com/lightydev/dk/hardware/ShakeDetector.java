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

package com.lightydev.dk.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.FloatMath;

/**
 * @author Daniel Serdyukov
 * @since 2.3.1
 */
public class ShakeDetector implements SensorEventListener {

  private static final int SHAKE_SLOP_TIME = 1000;

  private static final float SHAKE_THRESHOLD_GRAVITY = 2f;

  private final OnShakeListener mShakeListener;

  private final float mShakeThresholdGravity;

  private long mShakeSlopTime;

  private float mLastShakeGravity;

  private long mLastShakeTime;

  public ShakeDetector(OnShakeListener l) {
    this(l, SHAKE_THRESHOLD_GRAVITY);
  }

  public ShakeDetector(OnShakeListener l, float thresholdGravity) {
    mShakeListener = l;
    mShakeThresholdGravity = thresholdGravity;
    mShakeSlopTime = SHAKE_SLOP_TIME;
  }

  public void register(Context context) {
    final SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
  }

  public void unregister(Context context) {
    final SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (mShakeListener != null) {
      final float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
      final float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
      final float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;
      final float gShake = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);
      if (gShake >= mShakeThresholdGravity) {
        final long now = SystemClock.uptimeMillis();
        if (mLastShakeTime + mShakeSlopTime > now) {
          return;
        }
        mLastShakeGravity = gShake;
        mLastShakeTime = now;
        mShakeListener.onShake(this);
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  public float getShakeGravity() {
    return mLastShakeGravity;
  }

  public long getShakeTime() {
    return mLastShakeTime;
  }

  public interface OnShakeListener {
    void onShake(ShakeDetector detector);
  }

}
