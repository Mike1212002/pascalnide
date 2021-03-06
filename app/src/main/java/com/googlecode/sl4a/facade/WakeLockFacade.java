/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.sl4a.facade;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.duy.pascal.interperter.libraries.PascalLibrary;
import com.duy.pascal.interperter.libraries.android.AndroidLibraryManager;
import com.duy.pascal.interperter.libraries.annotations.PascalMethod;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContextMixin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A facade exposing some of the functionality of the PowerManager, in particular wake locks.
 *
 * @author Felix Arends (felixarends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class WakeLockFacade extends PascalLibrary {

    private final static String WAKE_LOCK_TAG =
            "com.googlecode.android_scripting.facade.PowerManagerFacade";
    private final WakeLockManager mManager;

    public WakeLockFacade(AndroidLibraryManager manager) {
        mManager = new WakeLockManager(manager.getContext());
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Acquires a full wake lock (CPU on, screen bright, keyboard bright).")
    public void wakeLockAcquireFull() {
        mManager.acquire(WakeLockType.FULL);
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Acquires a partial wake lock (CPU on).")
    public void wakeLockAcquirePartial() {
        mManager.acquire(WakeLockType.PARTIAL);
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Acquires a bright wake lock (CPU on, screen bright).")
    public void wakeLockAcquireBright() {
        mManager.acquire(WakeLockType.BRIGHT);
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Acquires a dim wake lock (CPU on, screen dim).")
    public void wakeLockAcquireDim() {
        mManager.acquire(WakeLockType.DIM);
    }

    @PascalMethod(description = "Releases the wake lock.")
    public void wakeLockRelease() {
        mManager.release();
    }

    @Override
    public void onFinalize() {
        wakeLockRelease();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void declareConstants(ExpressionContextMixin context) {

    }

    @Override
    public void declareTypes(ExpressionContextMixin context) {

    }

    @Override
    public void declareVariables(ExpressionContextMixin context) {

    }

    @Override
    public void declareFunctions(ExpressionContextMixin context) {

    }

    private enum WakeLockType {
        FULL, PARTIAL, BRIGHT, DIM
    }

    private class WakeLockManager {
        private final PowerManager mmPowerManager;
        private final Map<WakeLockType, WakeLock> mmLocks = new HashMap<>();

        public WakeLockManager(Context context) {
            mmPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            addWakeLock(WakeLockType.PARTIAL, PowerManager.PARTIAL_WAKE_LOCK);
            addWakeLock(WakeLockType.FULL, PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE);
            addWakeLock(WakeLockType.BRIGHT, PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE);
            addWakeLock(WakeLockType.DIM, PowerManager.SCREEN_DIM_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE);
        }

        private void addWakeLock(WakeLockType type, int flags) {
            WakeLock full = mmPowerManager.newWakeLock(flags, WAKE_LOCK_TAG);
            full.setReferenceCounted(false);
            mmLocks.put(type, full);
        }

        public void acquire(WakeLockType type) {
            mmLocks.get(type).acquire();
            for (Entry<WakeLockType, WakeLock> entry : mmLocks.entrySet()) {
                if (entry.getKey() != type) {
                    entry.getValue().release();
                }
            }
        }

        public void release() {
            for (Entry<WakeLockType, WakeLock> entry : mmLocks.entrySet()) {
                entry.getValue().release();
            }
        }
    }
}
