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

package com.duy.pascal.interperter.source;

import android.support.annotation.Nullable;

import java.io.Reader;

/**
 * Created by Duy on 11/3/2017.
 */

public class SourceCode implements ScriptSource {

    @Nullable
    @Override
    public String[] list() {
        return new String[0];
    }

    @Nullable
    @Override
    public Reader read(String fileName) {
        return null;
    }
}
