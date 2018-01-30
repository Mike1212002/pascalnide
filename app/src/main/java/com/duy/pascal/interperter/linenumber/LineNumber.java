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

package com.duy.pascal.interperter.linenumber;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;


public class LineNumber implements Serializable, Cloneable, Comparable<LineNumber> {
    public static final LineNumber SYSTEM_LINE = new LineNumber(-1, "system");
    public static final LineNumber ANONYMOUS = new LineNumber(-1, "anonymous");

    private int line;
    private int column;
    private int length;
    @Nullable
    private String sourceFile;

    public LineNumber(int line, @NonNull String sourceFile) {
        this.length = -1;
        this.line = line;
        this.sourceFile = sourceFile;
    }

    public LineNumber(int line, int column, @NonNull String sourceFile) {
        this.length = -1;
        this.line = line;
        this.column = column;
        this.sourceFile = sourceFile;
    }

    public LineNumber(int line, int column, int length, @NonNull String sourceFile) {
        this.length = -1;
        this.line = line;
        this.column = column;
        this.sourceFile = sourceFile;
        this.length = length;
    }

    public int getLine() {
        return this.line;
    }

    public void setLine(int var1) {
        this.line = var1;
    }

    public Integer getColumn() {
        return this.column;
    }

    public void setColumn(int var1) {
        this.column = var1;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int var1) {
        this.length = var1;
    }

    @Nullable
    public String getSourceFile() {
        return this.sourceFile;
    }

    @NonNull
    public String toString() {
        return "Line " + this.line + (this.column >= 0 ? ":" + this.column : "") + " " + this.sourceFile;
    }

    @Override
    public int compareTo(@NonNull LineNumber o) {
        if (this.getLine() < o.getLine()) {
            return -1; //less than
        }
        if (getLine() > o.getLine()) {
            return 1; //greater than
        }
        //line is equal
        return this.getColumn().compareTo(o.getColumn());
    }
}
