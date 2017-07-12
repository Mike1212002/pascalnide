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

package com.duy.pascal.frontend.setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.duy.pascal.frontend.R;

import static com.duy.pascal.frontend.setting.PreferencesUtil.bindPreferenceSummaryToValue;


public class FragmentSetting extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_pref_font_size)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_pref_font_size)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_code_theme)));
//        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_pref_font)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_pref_lang)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_max_page)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_max_history_edit)));

        //console region
        bindPreferenceSummaryToValue(findPreference("key_pref_console_font_size"));
        bindPreferenceSummaryToValue(findPreference("pref_console_bg_color"));
        bindPreferenceSummaryToValue(findPreference("key_pref_tab_width"));

        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_console_frame_rate)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.key_console_max_buffer_size)));

    }
}
