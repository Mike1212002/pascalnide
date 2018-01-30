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
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.duy.pascal.interperter.exceptions.parsing.ParsingException;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.tokens.Token;
import com.duy.pascal.ui.R;
import com.duy.pascal.ui.code.ExceptionManager;

import static com.duy.pascal.ui.code.ExceptionManager.formatMessageFromResource;

public class GroupingException extends ParsingException {
    private Type exceptionTypes;
    @Nullable
    private Token openToken;
    @Nullable
    private Token closeToken;
    @Nullable
    private Exception caused;

    public GroupingException(@Nullable LineNumber line, @NonNull String message) {
        super(line, message);
    }

    public GroupingException(@Nullable LineNumber line, @NonNull Type exceptionTypes, @NonNull Token openToken, @NonNull Token closeToken) {
        super(line);
        this.exceptionTypes = exceptionTypes;
        this.openToken = openToken;
        this.closeToken = closeToken;
    }

    public GroupingException(@Nullable LineNumber line, @NonNull Type exceptionTypes) {
        super(line);
        this.exceptionTypes = exceptionTypes;
    }

    @Nullable
    public Type getExceptionTypes() {
        return this.exceptionTypes;
    }

    @Nullable
    public Token getOpenToken() {
        return this.openToken;
    }

    @Nullable
    public Token getCloseToken() {
        return this.closeToken;
    }

    @Nullable
    public Exception getCaused() {
        return this.caused;
    }

    public void setCaused(@Nullable Exception var1) {
        this.caused = var1;
    }

    @Nullable
    public String getMessage() {
        if (exceptionTypes != null) {
            return exceptionTypes.message + ": " + (caused != null ? caused.getMessage() : "");
        }
        if (caused != null) {
            return caused.getMessage();
        }
        return super.getMessage();
    }

    public boolean canQuickFix() {
        return this.exceptionTypes == Type.UNFINISHED_BEGIN_END;
    }

    @Override
    public Spanned getFormattedMessage(@NonNull Context context) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(ExceptionManager.formatLine(context, this.getLineNumber())).append("\n\n");

        if (getExceptionTypes().equals(GroupingException.Type.EXTRA_END)) {
            Token openToken = this.getOpenToken();
            Spanned message = formatMessageFromResource(this, context, R.string.unbalance_end,
                    openToken.toString(),
                    openToken.getLineNumber().getLine(),
                    openToken.getLineNumber().getColumn(),
                    getCloseToken().toString(),
                    getLineNumber().getLine(),
                    getLineNumber().getColumn());
            builder.append(message);
        } else {
            String message = getEnumeratedGroupingException(context, this);
            builder.append(message);
        }
        return builder;
    }

    private String getEnumeratedGroupingException(Context context, GroupingException e) {
        GroupingException.Type exceptionTypes = e.getExceptionTypes();
        if (exceptionTypes == GroupingException.Type.IO_EXCEPTION) {
            return (context.getString(R.string.IO_EXCEPTION));
        } else if (exceptionTypes == GroupingException.Type.EXTRA_END) {
            return (context.getString(R.string.unbalance_end));
        } else if (exceptionTypes == GroupingException.Type.INCOMPLETE_CHAR) {
            return (context.getString(R.string.INCOMPLETE_CHAR));
        } else if (exceptionTypes == GroupingException.Type.MISMATCHED_BEGIN_END) {
            return (context.getString(R.string.MISMATCHED_BEGIN_END));
        } else if (exceptionTypes == GroupingException.Type.MISMATCHED_BRACKETS) {
            return (context.getString(R.string.MISMATCHED_BRACKETS));
        } else if (exceptionTypes == GroupingException.Type.MISMATCHED_PARENTHESES) {
            return (context.getString(R.string.MISMATCHED_PARENTHESES));
        } else if (exceptionTypes == GroupingException.Type.UNFINISHED_BEGIN_END) {
            return (context.getString(R.string.UNFINISHED_BEGIN_END));
        } else if (exceptionTypes == GroupingException.Type.UNFINISHED_PARENTHESES) {
            return (context.getString(R.string.UNFINISHED_PARENTHESES));
        } else if (exceptionTypes == GroupingException.Type.UNFINISHED_BRACKETS) {
            return (context.getString(R.string.UNFINISHED_BRACKETS));
        } else if (exceptionTypes == GroupingException.Type.MISSING_INCLUDE) {
            return (context.getString(R.string.MISSING_INCLUDE));
        } else if (exceptionTypes == GroupingException.Type.NEWLINE_IN_QUOTES) {
            return (context.getString(R.string.NEWLINE_IN_QUOTES));
        }
        return (e.getLocalizedMessage());
    }

    public enum Type {
        MISMATCHED_PARENTHESES("Mismatched parentheses"),
        MISMATCHED_BRACKETS("Mismatched brackets"),
        MISMATCHED_BEGIN_END("Mismatched begin - end construct"),
        UNFINISHED_BEGIN_END("Unfinished begin - end construct"),
        UNFINISHED_PARENTHESES("You forgot to close your parentheses"),
        UNFINISHED_BRACKETS("You forgot to close your brackets"),
        EXTRA_END("You have an extra 'end' in your program"),
        UNFINISHED_CONSTRUCT("You forgot to complete the structure you started here"),
        IO_EXCEPTION("IOException occurred while reading the input"),
        INCOMPLETE_CHAR("Incomplete character literal"),
        MISSING_INCLUDE("Missing file to include"),
        NEWLINE_IN_QUOTES("You must close your quotes before starting a new line");

        String message;

        Type(String message) {
            this.message = message;
        }
    }
}
