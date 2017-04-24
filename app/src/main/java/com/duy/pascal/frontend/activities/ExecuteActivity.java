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

package com.duy.pascal.frontend.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.duy.pascal.frontend.R;
import com.duy.pascal.frontend.code.CompileManager;
import com.duy.pascal.frontend.code.ExceptionManager;
import com.duy.pascal.frontend.dialog.DialogManager;
import com.duy.pascal.frontend.view.exec_screen.console.ConsoleView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ExecuteActivity extends AbstractExecActivity {
    @BindView(R.id.console)
    public ConsoleView mConsoleView;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        ButterKnife.bind(this);
        setupActionBar();
        getConsoleView().updateSize();
        getConsoleView().showPrompt();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            filePath = extras.getString(CompileManager.FILE_PATH);
            if (filePath == null || filePath.isEmpty()) return;
            File file = new File(filePath);
            if (!file.exists()) {
                finish();
                return;
            }
            setTitle(file.getName());
            setEnableDebug(false); //disable debug
            createAndRunProgram(filePath); //execute file
        } else {
            finish();
        }
    }

    @Override
    public void debugProgram() {

    }


    private void setupActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_console, menu);
//        menu.findItem(R.id.action_next_line).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_soft:
                showKeyBoard();
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_change_keyboard:
                changeKeyBoard();
                break;
            case R.id.action_next_line:
                program.resume();
                break;
            case R.id.action_rerun:
                CompileManager.execute(this, filePath);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * show dialog pick keyboard
     */
    private void changeKeyBoard() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr != null) {
            mgr.showInputMethodPicker();
        }
    }

    /**
     * show soft keyboard
     */
    public void showKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(getConsoleView(), 0);
    }

    public void toggleSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void showDialogComplete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.complete)
                .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mMessageHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }
                })
                .setNegativeButton(R.string.view_console, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        try {
            builder.create().show();
        } catch (Exception ignored) {
        }
    }

    /**
     * show error compile or runtime
     */
    @Override
    public void onError(Exception e) {
        ExceptionManager exceptionManager = new ExceptionManager(this);
        DialogManager.createDialog(this, "Runtime error", exceptionManager.getMessage(e)).show();
        //debug
        if (DEBUG) e.printStackTrace();
    }


    @Override
    public ConsoleView getConsoleView() {
        return mConsoleView;
    }
}

