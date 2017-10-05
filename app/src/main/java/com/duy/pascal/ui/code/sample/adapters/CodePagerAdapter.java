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

package com.duy.pascal.ui.code.sample.adapters;

import android.support.v4.app.FragmentManager;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.v4.ArrayPagerAdapter;
import com.duy.pascal.ui.code.sample.fragments.FragmentCodeSample;

import java.util.List;

/**
 * Created by Duy on 28-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CodePagerAdapter extends ArrayPagerAdapter<FragmentCodeSample> {
    public CodePagerAdapter(FragmentManager fragmentManager, List<PageDescriptor> descriptors) {
        super(fragmentManager, descriptors);

    }

    @Override
    protected FragmentCodeSample createFragment(PageDescriptor desc) {
        return FragmentCodeSample.newInstance(desc.getTitle());
    }
}
