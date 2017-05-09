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

package com.googlecode.sl4a.interpreter;

import android.os.Environment;

/**
 * A collection of constants required for installation/removal of an interpreter.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class InterpreterConstants {

    public static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    public static final String SDCARD_SL4A_ROOT = SDCARD_ROOT + "sl4a/";

    public static final String SCRIPTS_ROOT = SDCARD_SL4A_ROOT + "scripts/";

}
