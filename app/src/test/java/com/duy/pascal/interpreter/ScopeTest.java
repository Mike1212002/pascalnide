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

package com.duy.pascal.interpreter;

/**
 * Created by Duy on 28-Aug-17.
 */

public class ScopeTest extends BaseTestCase {
    @Override
    public String getDirTest() {
        return "test_scope";
    }

    public void testtest_gobal() {
        run("test_gobal.pas");
    }

    public void testtest_gobal1() {
        run("test_gobal1.pas");
    }

    public void testtest_local() {
        run("test_local.pas");
    }

    public void testtest_local1() {
        run("test_local1.pas");
    }
}