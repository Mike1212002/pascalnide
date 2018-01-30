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

package com.duy.pascal.interperter.ast.codeunit;

import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.pascal.interperter.config.RunMode;
import com.duy.pascal.interperter.declaration.library.PascalUnitDeclaration;
import com.duy.pascal.interperter.declaration.program.PascalProgramDeclaration;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.ui.debug.CallStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RuntimePascalProgram extends RuntimeExecutableCodeUnit<PascalProgramDeclaration> {

    public RuntimePascalProgram(PascalProgramDeclaration declaration) throws RuntimePascalException {
        super(declaration);
    }

    @Override
    public void runImpl() throws RuntimePascalException {
        this.mode = RunMode.RUNNING;

        //generate init code of library
        HashMap<PascalUnitDeclaration, RuntimeUnitPascal> librariesMap = getDeclaration().getContext().getRuntimeUnitMap();
        Set<Map.Entry<PascalUnitDeclaration, RuntimeUnitPascal>> entries = librariesMap.entrySet();
        for (Map.Entry<PascalUnitDeclaration, RuntimeUnitPascal> entry : entries) {
            entry.getValue().runInit();
        }

        if (isDebug()) getDebugListener().onValueVariableChanged(new CallStack(this));
        getDeclaration().root.visit(this, this);

        //generate final code library
        for (Map.Entry<PascalUnitDeclaration, RuntimeUnitPascal> entry : entries) {
            entry.getValue().runFinal();
        }

        if (isDebug()) {
            getDebugListener().onValueVariableChanged(new CallStack(this));
            getDebugListener().onFinish();
        }
    }

    @Override
    public VariableContext getParentContext() {
        return null;
    }

    @Override
    public String toString() {
        return getDeclaration().getContext().getStartPosition().getSourceFile();
    }
}
