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

package com.duy.pascal.ui.code.sample.model;

/**
 * Created by Duy on 08-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CodeSampleEntry {
    /**
     * name of file code
     */
    private String name;

    /**
     * code
     */
    private String content;
    private String query;

    public CodeSampleEntry(String name, CharSequence content) {
        this.name = name;
        this.content = content.toString();
    }

    public CodeSampleEntry(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public CodeSampleEntry clone() {
        return new CodeSampleEntry(name, content);
    }
}
