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

package com.lightydev.dk.app;

import android.content.Intent;
import android.net.Uri;

import java.util.Locale;

/**
 * @author Daniel Serdyukov
 * @since 2.2.1
 */
public final class GoogleMapsIntents {

  private static final String MAPS_URL = "https://maps.google.com/maps";

  private GoogleMapsIntents() {
  }

  public static Intent openMaps() {
    return new Intent(Intent.ACTION_VIEW, Uri.parse(MAPS_URL));
  }

  public static Intent search(String query) {
    return new Intent(
        Intent.ACTION_VIEW,
        Uri.parse(String.format(MAPS_URL + "?q=%s", query))
    );
  }

  public static Intent withMarker(double lat, double lng) {
    return new Intent(
        Intent.ACTION_VIEW,
        Uri.parse(String.format(Locale.US, MAPS_URL + "?q=%f,%f", lat, lng))
    );
  }

  public static Intent routeFromTo(double srcLat, double srcLng, double destLat, double destLng) {
    return new Intent(
        Intent.ACTION_VIEW,
        Uri.parse(String.format(Locale.US, MAPS_URL + "?saddr=%f,%f&daddr=%f,%f", srcLat, srcLng, destLat, destLng))
    );
  }

  public static Intent routeTo(double lat, double lng) {
    return new Intent(
        Intent.ACTION_VIEW,
        Uri.parse(String.format(Locale.US, MAPS_URL + "?daddr=%f,%f", lat, lng))
    );
  }

}
