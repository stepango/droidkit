/*
 * Copyright 2012-2013 Daniel Serdyukov
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

package com.lightydev.dk.util;

import com.lightydev.dk.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public final class Reflect {

  private static final Map<Class<?>, Class<?>> BOXING = new ConcurrentHashMap<>();

  static {
    BOXING.put(Byte.class, byte.class);
    BOXING.put(Short.class, short.class);
    BOXING.put(Integer.class, int.class);
    BOXING.put(Long.class, long.class);
    BOXING.put(Float.class, float.class);
    BOXING.put(Double.class, double.class);
  }

  private Reflect() {
  }

  public static Method findMethod(Class<?> clazz, String methodName, Class<?>... args) throws NoSuchMethodException {
    do {
      try {
        return clazz.getDeclaredMethod(methodName, args);
      } catch (NoSuchMethodException e) {
        Logger.quiet("%s", e);
      }
    } while ((clazz = clazz.getSuperclass()) != null);
    throw new NoSuchMethodException(methodName);
  }

  public static Object invokeQuietly(Object object, String methodName, Object... args) {
    try {
      return invoke(object, findMethod(object.getClass(), methodName, getClasses(true, args)), args);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  public static boolean classEquals(Object o1, Object o2) {
    return o1 != null && o2 != null && o1.getClass() == o2.getClass();
  }

  public static int hashCode(Object o) {
    if (o != null) {
      return o.hashCode();
    }
    return 0;
  }

  private static Object invoke(Object object, Method method, Object... args) {
    method.setAccessible(true);
    try {
      return method.invoke(object, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private static Class<?>[] getClasses(boolean boxing, Object... args) {
    final Class<?>[] classes = new Class<?>[args.length];
    for (int i = 0; i < args.length; ++i) {
      final Class<?> clazz = args[i].getClass();
      if (boxing && BOXING.containsKey(clazz)) {
        classes[i] = BOXING.get(clazz);
      } else {
        classes[i] = clazz;
      }
    }
    return classes;
  }

}
