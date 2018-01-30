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

package com.duy.pascal.interperter.exceptions.parsing.define;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.declaration.Name;
import com.duy.pascal.interperter.exceptions.parsing.ParsingException;
import com.duy.pascal.interperter.linenumber.LineNumber;

public class MissingBodyFunctionException extends ParsingException {
    @NonNull
    private final Name funName;

    public MissingBodyFunctionException(@NonNull Name funName, @Nullable LineNumber line) {
        super(line);
        this.funName = funName;
    }

    @Nullable
    public String getMessage() {
        return "Forward declaration not solved " + this.funName;
    }

    @NonNull
    public final Name getFunName() {
        return this.funName;
    }
}