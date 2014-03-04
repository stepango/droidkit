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

package com.lightydev.dk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author Daniel Serdyukov
 */
public class SwipeToDismissListView extends ListView {

  public SwipeToDismissListView(Context context) {
    super(context);
  }

  public SwipeToDismissListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SwipeToDismissListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

}
