/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.interperter.libraries.android.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContextMixin;
import com.duy.pascal.interperter.libraries.PascalLibrary;
import com.duy.pascal.interperter.libraries.android.AndroidLibraryManager;
import com.duy.pascal.interperter.libraries.annotations.PascalMethod;
import com.duy.pascal.interperter.libraries.annotations.PascalParameter;
import com.googlecode.sl4a.rpc.RpcOptional;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("unused")
public class AndroidPreferencesLibrary extends PascalLibrary {

    private Context mContext;

    public AndroidPreferencesLibrary() {

    }

    public AndroidPreferencesLibrary(AndroidLibraryManager manager) {
        mContext = manager.getContext();
    }

    @PascalMethod(description = "Read a value from shared preferences")
    public Object prefGetValue(
            @PascalParameter(name = "key") String key,
            @PascalParameter(name = "filename", description = "Desired preferences file. If not defined, uses the default Shared Preferences.") @RpcOptional String filename) {
        SharedPreferences p = getPref(filename);
        return p.getAll().get(key);
    }

    @PascalMethod(description = "Write a value to shared preferences")
    public void prefPutValue(
            @PascalParameter(name = "key") String key,
            @PascalParameter(name = "value") Object value,
            @PascalParameter(name = "filename", description = "Desired preferences file. If not defined, uses the default Shared Preferences.") @RpcOptional String filename)
            throws IOException {
        if (filename == null || filename.equals("")) {
            throw new IOException("Can't write to default preferences.");
        }
        SharedPreferences p = getPref(filename);
        Editor e = p.edit();
        if (value instanceof Boolean) {
            e.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            e.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            e.putLong(key, (Integer) value);
        } else if (value instanceof Float) {
            e.putFloat(key, (Float) value);
        } else if (value instanceof Double) { // TODO: Not sure if this is a good idea
            e.putFloat(key, ((Double) value).floatValue());
        } else {
            e.putString(key, value.toString());
        }
        e.apply();
    }

    @PascalMethod(description = "Get list of Shared Preference Values", returns = "Map of key,value")
    public Map<String, ?> prefGetAll(
            @PascalParameter(name = "filename", description = "Desired preferences file. If not defined, uses the default Shared Preferences.") @RpcOptional String filename) {
        return getPref(filename).getAll();
    }

    private SharedPreferences getPref(String filename) {
        if (filename == null || filename.equals("")) {
            return PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        return mContext.getSharedPreferences(filename, 0);

    }

    @Override
    public void onFinalize() {

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
}
