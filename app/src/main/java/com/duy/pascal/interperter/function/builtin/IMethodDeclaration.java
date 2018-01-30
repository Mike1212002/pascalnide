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

import android.support.annotation.Nullable;

import com.duy.pascal.interperter.ast.expressioncontext.ExpressionContext;
import com.duy.pascal.interperter.ast.runtime.value.FunctionCall;
import com.duy.pascal.interperter.ast.runtime.value.RuntimeValue;
import com.duy.pascal.interperter.declaration.Name;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.declaration.lang.types.ArgumentType;
import com.duy.pascal.interperter.declaration.lang.types.Type;


public interface IMethodDeclaration {
    /**
     * @return simple name of method;
     */
    Name getName();

    FunctionCall generateCall(LineNumber line, RuntimeValue[] values,
                              ExpressionContext f) throws Exception;

    FunctionCall generatePerfectFitCall(LineNumber line,
                                        RuntimeValue[] values, ExpressionContext f)
            throws Exception;

    ArgumentType[] argumentTypes();

    /**
     * return type of method
     */
    Type returnType();

    /**
     * short description of method, it can be null
     */
    @Nullable
    String description();
}
