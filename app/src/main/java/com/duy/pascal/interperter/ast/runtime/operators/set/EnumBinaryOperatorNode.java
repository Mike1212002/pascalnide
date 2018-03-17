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

package com.duy.pascal.interperter.ast.runtime.operators.set;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.expressioncontext.CompileTimeContext;
import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.runtime.operators.BinaryOperatorNode;
import com.duy.pascal.interperter.ast.runtime.value.EnumElementValue;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.ast.runtime.value.access.ConstantAccess;
import com.duy.pascal.interperter.declaration.lang.types.BasicType;
import com.duy.pascal.interperter.declaration.lang.types.OperatorTypes;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;
import com.duy.pascal.interperter.declaration.lang.types.set.EnumGroupType;
import com.duy.pascal.interperter.exceptions.runtime.CompileException;
import com.duy.pascal.interperter.exceptions.runtime.arith.PascalArithmeticException;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.utils.NullSafety;


public class EnumBinaryOperatorNode extends BinaryOperatorNode {

    public EnumBinaryOperatorNode(RuntimeValue operon1, RuntimeValue operon2,
                                  OperatorTypes operator, LineNumber line) {
        super(operon1, operon2, operator, line);
    }


    @Nullable
    @Override
    public RuntimeType getRuntimeType(ExpressionContext context) throws Exception {
        switch (operatorType) {
            case EQUALS:
            case GREATEREQ:
            case GREATERTHAN:
            case LESSEQ:
            case LESSTHAN:
            case NOTEQUAL:
                return new RuntimeType(BasicType.Boolean, false);
            case PLUS:
            case MINUS:
                EnumGroupType type = (EnumGroupType) leftNode;
                return new RuntimeType(type, false);
            default:
                throw new CompileException();

        }
    }

    @Override
    public Object operate(Object value1, Object value2)
            throws PascalArithmeticException, CompileException {
        EnumElementValue v1 = (EnumElementValue) value1;
        EnumElementValue v2 = (EnumElementValue) value2;
        switch (operatorType) {
            case EQUALS:
                return v1.equals(v2);

            case NOTEQUAL:
                return !v1.equals(v2);

            case GREATEREQ:
                if (NullSafety.isNullValue(v1)) return true;
                if (NullSafety.isNullValue(v2)) return false;
                return v1.getIndex() >= v2.getIndex();

            case GREATERTHAN:
                return v1.getIndex() > v2.getIndex();

            case LESSEQ:
                if (NullSafety.isNullValue(v1)) return false;
                if (NullSafety.isNullValue(v2)) return true;
                return v1.getIndex() <= v2.getIndex();

            case LESSTHAN:
                return v1.getIndex() < v2.getIndex();

            default:
                throw new CompileException();
        }
    }

    @Override
    public RuntimeValue compileTimeExpressionFold(CompileTimeContext context)
            throws Exception {
        Object val = this.compileTimeValue(context);
        if (val != null) {
            return new ConstantAccess<>(val, line);

        } else {
            return new EnumBinaryOperatorNode(
                    leftNode.compileTimeExpressionFold(context),
                    rightNode.compileTimeExpressionFold(context), operatorType,
                    line);
        }
    }

    @Override
    public void setLineNumber(LineNumber lineNumber) {

    }

    @Override
    public boolean canDebug() {
        return true;
    }
}
