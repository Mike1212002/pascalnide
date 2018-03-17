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

package com.duy.pascal.interperter.ast.runtime.value;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.declaration.Name;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;
import com.duy.pascal.interperter.declaration.lang.types.set.EnumGroupType;

/**
 * Created by Duy on 25-May-17.
 */

public class EnumElementValue implements RuntimeValue, Comparable<EnumElementValue> {
    private Name name;
    private EnumGroupType type;
    private Integer value;

    private Integer index;
    private LineNumber lineNumber;

    public EnumElementValue(Name name, @NonNull EnumGroupType type,
                            @NonNull Integer index, @NonNull LineNumber lineNumber) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.lineNumber = lineNumber;
    }

    public EnumGroupType getEnumGroupType() {
        return type;
    }


    @Nullable
    @Override
    public Object getValue(VariableContext context, RuntimeExecutableCodeUnit<?> main) throws RuntimePascalException {
        return index;
    }

    @Nullable
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) throws Exception {
        return new RuntimeType(type, false);//this is a constant
    }

    @NonNull
    @Override
    public LineNumber getLineNumber() {
        return lineNumber;
    }

    @Override
    public void setLineNumber(LineNumber lineNumber) {

    }

    @Nullable
    @Override
    public Object compileTimeValue(CompileTimeContext context) throws Exception {
        return this;//this is a constant
    }

    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context) throws Exception {
        return this;
    }

    @Override
    public AssignableValue asAssignableValue(ExpressionContext context) {
        return null;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * uses for print to console
     */
    @Override
    public String toString() {
        return name.toString();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public int compareTo(@NonNull EnumElementValue o) {
        return this.getIndex().compareTo(o.getIndex());
    }
}
