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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.gsm.SmsManager;

import com.duy.pascal.backend.lib.PascalLibrary;
import com.duy.pascal.backend.lib.android.utils.AndroidLibraryManager;
import com.duy.pascal.backend.lib.annotations.PascalMethod;
import com.duy.pascal.backend.lib.annotations.PascalParameter;
import com.googlecode.sl4a.Log;
import com.googlecode.sl4a.rpc.RpcDefault;
import com.googlecode.sl4a.rpc.RpcOptional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides access to SMS related functionality.
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class SmsFacade implements PascalLibrary {

    private final ContentResolver mContentResolver;
    private final SmsManager mSms;

    public SmsFacade(AndroidLibraryManager manager) {
        Context mContext = manager.getContext();
        mContentResolver = mContext.getContentResolver();
        mSms = SmsManager.getDefault();
    }

    private String buildSelectionClause(boolean unreadOnly) {
        if (unreadOnly) {
            return "read = 0";
        }
        return "";
    }

    private Uri buildFolderUri(String folder) {
        Uri.Builder builder = Uri.parse("content://sms").buildUpon();
        builder.appendPath(folder);
        Uri uri = builder.build();
        Log.v("Built SMS URI: " + uri);
        return uri;
    }

    private Uri buildMessageUri(Integer id) {
        Uri.Builder builder = Uri.parse("content://sms").buildUpon();
        ContentUris.appendId(builder, id);
        return builder.build();
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Sends an SMS.")
    public void smsSend(
            @PascalParameter(name = "destinationAddress", description = "typically a phone number") String destinationAddress,
            @PascalParameter(name = "text") String text) {
        mSms.sendTextMessage(destinationAddress, null, text, null, null);
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Returns the number of messages.")
    public Integer smsGetMessageCount(@PascalParameter(name = "unreadOnly") Boolean unreadOnly,
                                      @PascalParameter(name = "folder") @RpcDefault("inbox") String folder) {
        Uri uri = buildFolderUri(folder);
        Integer result = 0;
        String selection = buildSelectionClause(unreadOnly);
        Cursor cursor = mContentResolver.query(uri, null, selection, null, null);
        if (cursor != null) {
            result = cursor.getCount();
            cursor.close();
        } else {
            result = 0;
        }
        return result;
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Returns a List of all message IDs.")
    public List<Integer> smsGetMessageIds(@PascalParameter(name = "unreadOnly") Boolean unreadOnly,
                                          @PascalParameter(name = "folder") @RpcDefault("inbox") String folder) {
        Uri uri = buildFolderUri(folder);
        List<Integer> result = new ArrayList<>();
        String selection = buildSelectionClause(unreadOnly);
        String[] columns = {"_id"};
        Cursor cursor = mContentResolver.query(uri, columns, selection, null, null);
        while (cursor != null && cursor.moveToNext()) {
            result.add(cursor.getInt(0));
        }
        if (cursor != null)
            cursor.close();
        return result;
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Returns a List of all messages.", returns = "a List of messages as Maps")
    public List<JSONObject> smsGetMessages(@PascalParameter(name = "unreadOnly") Boolean unreadOnly,
                                           @PascalParameter(name = "folder") @RpcDefault("inbox") String folder,
                                           @PascalParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
        List<JSONObject> result = new ArrayList<>();
        Uri uri = buildFolderUri(folder);
        String selection = buildSelectionClause(unreadOnly);
        String[] columns;
        if (attributes == null || attributes.length() == 0) {
            // In case no attributes are specified we set the default ones.
            columns = new String[]{"_id", "address", "date", "body", "read"};
        } else {
            // Convert selected attributes list into usable string list.
            columns = new String[attributes.length()];
            for (int i = 0; i < attributes.length(); i++) {
                columns[i] = attributes.getString(i);
            }
        }
        Cursor cursor = mContentResolver.query(uri, columns, selection, null, null);
        while (cursor != null && cursor.moveToNext()) {
            JSONObject message = new JSONObject();
            for (int i = 0; i < columns.length; i++) {
                message.put(columns[i], cursor.getString(i));
            }
            result.add(message);
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Returns message attributes.")
    public JSONObject smsGetMessageById(
            @PascalParameter(name = "id", description = "message ID") Integer id,
            @PascalParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
        JSONObject result = new JSONObject();
        Uri uri = buildMessageUri(id);
        String[] columns;
        if (attributes == null || attributes.length() == 0) {
            // In case no attributes are specified we set the default ones.
            columns = new String[]{"_id", "address", "date", "body", "read"};
        } else {
            // Convert selected attributes list into usable string list.
            columns = new String[attributes.length()];
            for (int i = 0; i < attributes.length(); i++) {
                columns[i] = attributes.getString(i);
            }
        }
        Cursor cursor = mContentResolver.query(uri, columns, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < columns.length; i++) {
                result.put(columns[i], cursor.getString(i));
            }
            cursor.close();
        } else {
            result = null;
        }
        return result;
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Returns a List of all possible message attributes.")
    public List<String> smsGetAttributes() {
        List<String> result = new ArrayList<>();
        Cursor cursor = mContentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        if (cursor != null) {
            String[] columns = cursor.getColumnNames();
            Collections.addAll(result, columns);
            cursor.close();
        } else {
            result = null;
        }
        return result;
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Deletes a message.", returns = "True if the message was deleted")
    public Boolean smsDeleteMessage(@PascalParameter(name = "id") Integer id) {
        Uri uri = buildMessageUri(id);
        Boolean result;
        result = mContentResolver.delete(uri, null, null) > 0;
        return result;
    }

    @SuppressWarnings("unused")
    @PascalMethod(description = "Marks messages as read.", returns = "number of messages marked read")
    public Integer smsMarkMessageRead(
            @PascalParameter(name = "ids", description = "List of message IDs to mark as read.") JSONArray ids,
            @PascalParameter(name = "read") Boolean read) throws JSONException {
        Integer result = 0;
        ContentValues values = new ContentValues();
        values.put("read", read);
        for (int i = 0; i < ids.length(); i++) {
            Uri uri = buildMessageUri(ids.getInt(i));
            result += mContentResolver.update(uri, values, null, null);
        }
        return result;
    }

    @Override
    public boolean instantiate(Map<String, Object> pluginargs) {
        return false;
    }

    @Override
    public void shutdown() {
    }
}