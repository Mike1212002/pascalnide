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

package com.duy.pascal.interperter.ast.runtime.value.boxing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.runtime.value.NullValue;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.debugable.DebuggableReturnValue;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;
import com.duy.pascal.interperter.declaration.lang.types.Type;
import com.duy.pascal.interperter.declaration.lang.types.set.SetType;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.utils.NullSafety;

import java.util.LinkedList;

public class SetBoxer extends DebuggableReturnValue {
    private LinkedList<RuntimeValue> values;
    private Type elementType;
    private LineNumber line;

    public SetBoxer(LinkedList<RuntimeValue> array,
                    Type elementType, LineNumber line) {
        this.values = array;
        this.elementType = elementType;
        this.line = line;
    }


    public LinkedList<RuntimeValue> getValues() {
        return values;
    }

    @NonNull
    @Override
    public LineNumber getLineNumber() {
        return line;
    }

    @Override
    public void setLineNumber(LineNumber lineNumber) {
        this.line = lineNumber;
    }

    @Nullable
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) throws Exception {
        SetType<Type> setType = new SetType<>(elementType, line);
        return new RuntimeType(setType, false);
    }

    @Override
    public boolean canDebug() {
        return false;
    }

    @Override
    public Object getValueImpl(VariableContext context, RuntimeExecutableCodeUnit<?> main)
            throws RuntimePascalException {
        LinkedList<Object> result = new LinkedList<>();
        for (RuntimeValue value : values) {
            result.add(value.getValue(context, main));
        }
        return result;
    }

    @Override
    public Object compileTimeValue(CompileTimeContext context)
            throws Exception {
        LinkedList<Object> result = new LinkedList<>();
        for (RuntimeValue value : values) {
            Object o = value.compileTimeValue(context);
            if (!NullSafety.isNullValue(o)) {
                result.add(o);
            } else {
                return NullValue.get();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context) throws Exception {
        LinkedList<RuntimeValue> result = new LinkedList<>();
        for (RuntimeValue value : values) {
            result.add(value.compileTimeExpressionFold(context));
        }
        return new SetBoxer(result, elementType, line);
    }
}
