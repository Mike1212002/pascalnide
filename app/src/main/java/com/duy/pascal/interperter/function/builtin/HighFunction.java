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

package com.duy.pascal.interperter.function.builtin;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.node.Node;
import com.duy.pascal.interperter.ast.runtime.value.FunctionCall;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.declaration.Name;
import com.duy.pascal.interperter.declaration.lang.types.ArgumentType;
import com.duy.pascal.interperter.declaration.lang.types.BasicType;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;
import com.duy.pascal.interperter.declaration.lang.types.Type;
import com.duy.pascal.interperter.declaration.lang.types.set.ArrayType;
import com.duy.pascal.interperter.declaration.lang.types.set.EnumGroupType;
import com.duy.pascal.interperter.declaration.lang.types.subrange.IntegerRange;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.linenumber.LineNumber;

public class HighFunction implements IMethodDeclaration {
    private RuntimeType runtimeType;
    private ArgumentType[] argumentTypes = {new RuntimeType(BasicType.create(Object.class), false)};

    @Override
    public Name getName() {
        return Name.create("High");
    }

    @Override
    public FunctionCall generateCall(LineNumber line, RuntimeValue[] arguments,
                                     ExpressionContext f) throws Exception {
        RuntimeValue value = arguments[0];
        this.runtimeType = value.getRuntimeType(f);
        return new HighCall(value, line);
    }

    @Override
    public FunctionCall generatePerfectFitCall(LineNumber line, RuntimeValue[] values, ExpressionContext f) throws Exception {
        return generateCall(line, values, f);
    }

    @Override
    public ArgumentType[] argumentTypes() {
        return argumentTypes;
    }

    @Override
    public Type returnType() {
        if (runtimeType != null) {
            if (runtimeType.declType instanceof ArrayType) {
                return BasicType.Integer;
            } else {
                return BasicType.create(Object.class);
            }
        } else {
            return BasicType.create(Object.class);
        }
    }

    @Override
    public String description() {
        return null;
    }

    private class HighCall extends BuiltinFunctionCall {

        private RuntimeValue value;
        private LineNumber line;

        HighCall(RuntimeValue value, LineNumber line) {
            this.value = value;
            this.line = line;
        }

        @Nullable
        @Override
        public RuntimeType getRuntimeType(ExpressionContext context) throws Exception {
            return new RuntimeType(HighFunction.this.returnType(), false);
        }

        @NonNull
        @Override
        public LineNumber getLineNumber() {
            return line;
        }

        @Override
        public void setLineNumber(LineNumber lineNumber) {

        }

        @Override
        public Object compileTimeValue(CompileTimeContext context) {
            return null;
        }

        @Override
        public RuntimeValue compileTimeExpressionFold(CompileTimeContext context)
                throws Exception {
            return new HighCall(value, line);
        }

        @Override
        public Node compileTimeConstantTransform(CompileTimeContext c)
                throws Exception {
            return new HighCall(value, line);
        }

        @Override
        protected String getFunctionNameImpl() {
            return "high";
        }

        @Override
        public Object getValueImpl(@NonNull VariableContext f, @NonNull RuntimeExecutableCodeUnit<?> main)
                throws RuntimePascalException {

            Type declType = runtimeType.declType;
            if (declType instanceof ArrayType) {
                IntegerRange bounds = ((ArrayType) declType).getBound();
                Object[] value = (Object[]) this.value.getValue(f, main);
                int size = value.length;
                if (bounds == null) {
                    return size - 1;
                } else {
                    return bounds.getFirst() + size - 1;
                }
            } else if (BasicType.Byte.equals(declType)) {
                return Byte.MAX_VALUE;
            } else if (BasicType.Short.equals(declType)) {
                return Short.MAX_VALUE;
            } else if (BasicType.Integer.equals(declType)) {
                return Integer.MAX_VALUE;
            } else if (BasicType.Long.equals(declType)) {
                return Long.MAX_VALUE;
            } else if (BasicType.Float.equals(declType)) {
                return Float.MAX_VALUE;
            } else if (BasicType.Double.equals(declType)) {
                return Double.MAX_VALUE;
            } else if (BasicType.Character.equals(declType)) {
                return Character.MAX_VALUE;
            } else if (declType instanceof EnumGroupType) {
                EnumGroupType enumGroupType = (EnumGroupType) declType;
                return enumGroupType.get(enumGroupType.getSize() - 1);
            }
            return null;
        }
    }
}
