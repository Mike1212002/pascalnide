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

package com.duy.pascal.interperter.ast.runtime.value.access;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.codeunit.RuntimeUnitPascal;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.debugable.DebuggableReturnValue;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;

/**
 * Created by Duy on 16-Jun-17.
 */

public class LibraryIdentifierAccess extends DebuggableReturnValue {

    private final RuntimeUnitPascal container;
    private final RuntimeValue value;
    private final LineNumber lineNumber;

    public LibraryIdentifierAccess(RuntimeUnitPascal container, RuntimeValue value, LineNumber lineNumber) {
        this.container = container;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    @Override
    public boolean canDebug() {
        return false;
    }

    @Override
    public Object getValueImpl(VariableContext context, RuntimeExecutableCodeUnit<?> main) throws RuntimePascalException {
        return value.getValue(container, main);
    }

    @Nullable
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) throws Exception {
        return value.getRuntimeType(context);
    }

    @NonNull
    @Override
    public LineNumber getLineNumber() {
        return lineNumber;
    }

    @Nullable
    @Override
    public Object compileTimeValue(CompileTimeContext context) throws Exception {
        return value.compileTimeValue(context);
    }

    @Nullable
    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context) throws Exception {
        return null;
    }
}
