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

package com.duy.pascal.interperter.exceptions.parsing.grouping;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spanned;

import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.ui.R;

import static com.duy.pascal.ui.code.ExceptionManager.formatMessageFromResource;

public class StrayCharacterException extends GroupingException {
    private final char charCode;

    public StrayCharacterException(@NonNull LineNumber line, char charCode) {
        super(line, "Stray character in program: " + charCode + "\nChar code " + charCode);
        this.charCode = charCode;
    }

    public char getCharCode() {
        return this.charCode;
    }

    @Override
    public Spanned getFormattedMessage(@NonNull Context context) {
        return formatMessageFromResource(
                this,
                context,
                R.string.StrayCharacterException,
                getCharCode());
    }
}
