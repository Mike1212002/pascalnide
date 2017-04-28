/*
 *  Copyright 2017 Tran Le Duy
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

package com.duy.pascal.frontend.info_application;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.duy.pascal.frontend.R;
import com.duy.pascal.frontend.activities.AbstractAppCompatActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class InfoActivity extends AbstractAppCompatActivity {
    private static final String TAG = InfoActivity.class.getClass().getSimpleName();
    @BindView(R.id.list_translate)
    RecyclerView mListTranslate;
    @BindView(R.id.list_license)
    RecyclerView mListLicense;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setContentView(R.layout.activity_info);
        ButterKnife.bind(InfoActivity.this);
        setupActionBar();
        initContent();
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        setTitle(R.string.information);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initContent() {
        new TaskLoadData().execute();
        Log.d(TAG, "initContent: ");

    }

    @OnClick(R.id.gotoNcalcApp)
    public void clickCalc(View view) {
        gotoNcalcApp(view);
    }

    @OnClick(R.id.gotoSortApp)
    public void clickSort(View view) {
        gotoSortApp(view);
    }

    public void gotoNcalcApp(View view) {
        Uri uri = Uri.parse("market://details?id=com.duy.calculator.free");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |

                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=market://details?id=com.duy.calculator.free")));
        }
    }

    public void gotoSortApp(View view) {
        Uri uri = Uri.parse("market://details?id=com.duy.sortalgorithm.free");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |

                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.duy.sortalgorithm.free")));
        }
    }

    class TaskLoadData extends AsyncTask<Void, Void, Void> {
        ArrayList<ItemInfo> dataTranslate;
        ArrayList<ItemInfo> dataLicense;


        @Override
        protected Void doInBackground(Void... params) {
            dataTranslate = InfoAppUtil.readListTranslate(getResources().openRawResource(R.raw.help_translate));
            dataLicense = InfoAppUtil.readListLicense(getResources().openRawResource(R.raw.license));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            HelpTranslateAdapter adapterTranslate = new HelpTranslateAdapter(InfoActivity.this, dataTranslate);
            mListTranslate.setLayoutManager(new LinearLayoutManager(InfoActivity.this));
            mListTranslate.setHasFixedSize(false);
            mListTranslate.setAdapter(adapterTranslate);
            mListTranslate.setNestedScrollingEnabled(false);

            LicenseAdapter adapterLicense = new LicenseAdapter(InfoActivity.this, dataLicense);
            mListLicense.setLayoutManager(new LinearLayoutManager(InfoActivity.this));
            mListLicense.setHasFixedSize(false);
            mListLicense.setAdapter(adapterLicense);
            mListLicense.addItemDecoration(new DividerItemDecoration(InfoActivity.this, DividerItemDecoration.VERTICAL));
            mListLicense.setNestedScrollingEnabled(false);
        }
    }

}
