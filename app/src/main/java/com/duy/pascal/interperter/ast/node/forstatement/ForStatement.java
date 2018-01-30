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

package com.duy.pascal.interperter.ast.node.forstatement;

import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.node.Node;
import com.duy.pascal.interperter.ast.runtime.value.AssignableValue;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.declaration.lang.types.OperatorTypes;
import com.duy.pascal.interperter.declaration.lang.types.RuntimeType;
import com.duy.pascal.interperter.declaration.lang.types.Type;
import com.duy.pascal.interperter.declaration.lang.types.set.ArrayType;
import com.duy.pascal.interperter.declaration.lang.types.set.EnumGroupType;
import com.duy.pascal.interperter.declaration.lang.types.set.SetType;
import com.duy.pascal.interperter.exceptions.parsing.ParsingException;
import com.duy.pascal.interperter.exceptions.parsing.convert.UnConvertibleTypeException;
import com.duy.pascal.interperter.exceptions.parsing.define.UnknownIdentifierException;
import com.duy.pascal.interperter.exceptions.parsing.syntax.ExpectDoTokenException;
import com.duy.pascal.interperter.exceptions.parsing.syntax.ExpectedTokenException;
import com.duy.pascal.interperter.exceptions.parsing.syntax.WrongStatementException;
import com.duy.pascal.interperter.exceptions.parsing.value.UnAssignableTypeException;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.tokens.OperatorToken;
import com.duy.pascal.interperter.tokens.Token;
import com.duy.pascal.interperter.tokens.basic.AssignmentToken;
import com.duy.pascal.interperter.tokens.basic.BasicToken;
import com.duy.pascal.interperter.tokens.basic.DoToken;
import com.duy.pascal.interperter.tokens.basic.DowntoToken;
import com.duy.pascal.interperter.tokens.basic.ToToken;
import com.duy.pascal.interperter.tokens.grouping.GrouperToken;

/**
 * Created by Duy on 19-Jun-17.
 */

public class ForStatement {
    /**
     * create executable for statement
     */
    public static Node generateForNode(GrouperToken group, ExpressionContext context,
                                       LineNumber lineNumber) throws Exception {
        RuntimeValue identifier = null;
        try {
            identifier = group.getNextTerm(context);
        } catch (UnknownIdentifierException e) {
            throw ParsingException.makeVariableIdentifierExpectException(e, group, context);
        }
        AssignableValue varAssignable = identifier.asAssignableValue(context);
        RuntimeType varType = identifier.getRuntimeType(context);

        if (varAssignable == null) {
            throw new UnAssignableTypeException(identifier);
        }
        Token next = group.take();
        if (!(next instanceof AssignmentToken || next instanceof OperatorToken)) {
            throw new ExpectedTokenException(next, ":=", "in");
        }
        Node result;
        //case: for i :=
        if (next instanceof AssignmentToken) {
            RuntimeValue firstValue = group.getNextExpression(context);
            RuntimeValue convert = varType.getRawType().convert(firstValue, context);
            if (convert == null) {
                throw new UnConvertibleTypeException(firstValue, varType.getRawType(),
                        firstValue.getRuntimeType(context).declType, identifier, context);
            }
            firstValue = convert;

            next = group.take();
            boolean isDownto = false;
            //case: for i := ... to[downto] ..
            if (next instanceof DowntoToken) {
                isDownto = true;
            } else if (!(next instanceof ToToken)) {
                throw new ExpectedTokenException(next, "to", "downto");
            }
            RuntimeValue lastValue = group.getNextExpression(context);
            convert = varType.getRawType().convert(lastValue, context);
            if (convert == null) {
                throw new UnConvertibleTypeException(lastValue, varType.getRawType(),
                        lastValue.getRuntimeType(context).declType, identifier, context);
            }
            lastValue = convert;
            next = group.take();

            if (!(next instanceof DoToken)) {
                if (next instanceof BasicToken) {
                    throw new ExpectedTokenException("do", next);
                } else {
                    throw new ExpectDoTokenException(next.getLineNumber(), WrongStatementException.Statement.FOR_TO_DO);
                }
            }

            if (varType.getRawType() instanceof EnumGroupType) {
                Node command = group.getNextCommand(context);
                result = new ForEnumNode(varAssignable, firstValue,
                        lastValue, command,
                        (EnumGroupType) varType.getRawType(), lineNumber, isDownto);
            } else {
                Node command = group.getNextCommand(context);
                result = new ForNumberNode(context, varAssignable, firstValue,
                        lastValue, command, lineNumber, isDownto);
            }
        } else {
            //case: for <var> in <range>
            if (((OperatorToken) next).type == OperatorTypes.IN) {
                //assign value
                RuntimeValue enumList = group.getNextExpression(context);
                Type enumType = enumList.getRuntimeType(context).declType; //type of var

                //accept foreach : enum, set, array
                if (!(enumType instanceof EnumGroupType
                        || enumType instanceof ArrayType
                        || enumType instanceof SetType)) {
                    throw new UnConvertibleTypeException(enumList, varType.declType, enumType, context);
                }

                if (enumType instanceof EnumGroupType) {
                    RuntimeValue converted = varType.convert(enumList, context);
                    if (converted == null) {
                        throw new UnConvertibleTypeException(enumList,
                                varType.declType, enumType, context);
                    }
                } else if (enumType instanceof ArrayType) { //array type
                    ArrayType arrayType = (ArrayType) enumType;
                    RuntimeValue convert = arrayType.getElementType().convert(identifier, context);
                    if (convert == null) {
                        throw new UnConvertibleTypeException(identifier, arrayType.getElementType(), varType.declType, context);
                    }
                } else {
                    SetType setType = (SetType) enumType;
                    RuntimeValue convert = setType.getElementType().convert(identifier, context);
                    if (convert == null) {
                        throw new UnConvertibleTypeException(identifier, setType.getElementType(), varType.declType, context);
                    }
                }

                //check do token
                if (!(group.peek() instanceof DoToken)) {
                    throw new ExpectedTokenException(new DoToken(null), group.peek());
                }
                group.take(); //ignore do token
                //statement
                Node command = group.getNextCommand(context);
                return new ForInNode(varAssignable, enumList, command, lineNumber);
            } else {
                throw new ExpectedTokenException(next, ":=", "in");
            }
        }

        return result;
    }


}
