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

package com.duy.pascal.interperter.declaration.program;

import android.support.annotation.NonNull;

import com.duy.pascal.interperter.ast.codeunit.ExecutableCodeUnit;
import com.duy.pascal.interperter.ast.codeunit.RuntimeExecutableCodeUnit;
import com.duy.pascal.interperter.ast.codeunit.RuntimePascalProgram;
import com.duy.pascal.interperter.ast.node.Node;
import com.duy.pascal.interperter.exceptions.DiagnosticCollector;
import com.duy.pascal.interperter.exceptions.parsing.define.MultipleDefinitionsMainException;
import com.duy.pascal.interperter.exceptions.parsing.missing.MissingDotTokenException;
import com.duy.pascal.interperter.exceptions.runtime.RuntimePascalException;
import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.interperter.source.ScriptSource;
import com.duy.pascal.interperter.tokens.Token;
import com.duy.pascal.interperter.tokens.basic.PeriodToken;
import com.duy.pascal.interperter.tokens.basic.ProgramToken;
import com.duy.pascal.interperter.tokens.grouping.GrouperToken;
import com.duy.pascal.ui.runnable.IProgramHandler;

import java.util.List;

public class PascalProgramDeclaration extends ExecutableCodeUnit {
    public Node root;

    public PascalProgramDeclaration(ScriptSource source, List<ScriptSource> include,
                                    IProgramHandler handler, DiagnosticCollector collector)
            throws Exception {
        super(source, include, handler, collector);
    }

    @Override
    protected PascalProgramExpressionContext createExpressionContext(IProgramHandler handler) {
        return new PascalProgramExpressionContext(this, handler);
    }

    @Override
    public RuntimeExecutableCodeUnit<PascalProgramDeclaration> generate() throws RuntimePascalException {
        return new RuntimePascalProgram(this);
    }

    protected class PascalProgramExpressionContext extends CodeUnitExpressionContext {
        PascalProgramExpressionContext(PascalProgramDeclaration root, @NonNull IProgramHandler handler) {
            super(root, handler);
            root.getConfig().setLibrary(false);
        }

        @NonNull
        @Override
        public LineNumber getStartPosition() {
            return new LineNumber(0, getSourceName());
        }

        @Override
        protected boolean handleUnrecognizedDeclarationImpl(Token next, GrouperToken grouperToken)
                throws Exception {
            if (next instanceof ProgramToken) {
                /*programName =*/
                grouperToken.nextWordValue();
                grouperToken.assertNextSemicolon();
                return true;
            }
            return false;
        }


        @Override
        public void handleBeginEnd(GrouperToken i) throws Exception {
            if (PascalProgramDeclaration.this.root != null) {
                throw new MultipleDefinitionsMainException(i.peek().getLineNumber());
            }
            PascalProgramDeclaration.this.root = i.getNextCommand(this);
            if (!(i.peek() instanceof PeriodToken)) {
                throw new MissingDotTokenException(i.peek().getLineNumber());
            }
            i.take();
        }
    }

}
