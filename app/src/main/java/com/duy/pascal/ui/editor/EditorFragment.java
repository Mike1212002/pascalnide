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

package com.duy.pascal.ui.editor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.ui.EditorControl;
import com.duy.pascal.ui.R;
import com.duy.pascal.ui.autocomplete.autofix.command.AutoFixCommand;
import com.duy.pascal.ui.code.CompileManager;
import com.duy.pascal.ui.editor.indention.PascalCodeFormatter;
import com.duy.pascal.ui.editor.interfaces.ICodeEditor;
import com.duy.pascal.ui.editor.view.EditorView;
import com.duy.pascal.ui.editor.view.LineUtils;
import com.duy.pascal.ui.file.FileManager;
import com.duy.pascal.ui.utils.DLog;
import com.duy.pascal.ui.view.LockableScrollView;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by Duy on 15-Mar-17.
 * Editor fragment
 */
public class EditorFragment extends Fragment implements ICodeEditor {
    private static final String TAG = "EditorFragment";
    private final Handler mHandler = new Handler();

    private EditorView mCodeEditor;
    @Nullable
    private LockableScrollView mScrollView;
    private FileManager mFileManager;
    private LoadCodeTask mLoadCodeTask;

    public static EditorFragment newInstance(String filePath) {
        EditorFragment editorFragment = new EditorFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CompileManager.EXTRA_FILE, new File(filePath));
        editorFragment.setArguments(bundle);
        return editorFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileManager = new FileManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCodeEditor = view.findViewById(R.id.code_editor);
        mCodeEditor.setDropDownAnchorId(R.id.app_bar);
        mCodeEditor.setSrcPath(new File(getFilePath()).getName());
        mScrollView = view.findViewById(R.id.vertical_scroll);

        try {
            mCodeEditor.setEditorControl((EditorControl) getActivity());
        } catch (Exception ignored) {
        }

        if (mScrollView != null) {
            mCodeEditor.setVerticalScroll(mScrollView);
            mScrollView.setScrollListener(new LockableScrollView.ScrollListener() {
                @Override
                public void onScroll(int x, int y) {
                    mCodeEditor.updateTextHighlight();
                }
            });
        }
        mLoadCodeTask = new LoadCodeTask(mCodeEditor);
        mLoadCodeTask.execute((File) getArguments().getSerializable(CompileManager.EXTRA_FILE));
    }


    @Override
    public void onDestroyView() {
        DLog.d(TAG, "onDestroyView() called");
        if (mLoadCodeTask != null) {
            mLoadCodeTask.cancel(true);
        }
        if (isAutoSave()) saveFile();
        if (mCodeEditor != null && getFilePath() != null) {
            DLog.d(TAG, "onDestroyView: save edit history " + getFilePath());
            mCodeEditor.saveHistory(getFilePath());
        }
        super.onDestroyView();
    }

    private boolean isAutoSave() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeEditor.updateFromSettings();
        mCodeEditor.restoreHistory(getFilePath());
    }

    public void executeCommand(@NonNull AutoFixCommand command) {
        command.execute(mCodeEditor);
    }

    @Override
    public void saveAs() {

    }

    @Override
    public void findAndReplace(@NonNull String from, @NonNull String to, boolean regex, boolean matchCase) {
        mCodeEditor.replaceAll(from, to, regex, matchCase);
    }

    @Override
    public void find(@NonNull String find, boolean regex, boolean wordOnly, boolean matchCase) {
        mCodeEditor.find(find, regex, wordOnly, matchCase);
    }

    @Override
    public void saveFile() {
        if (mCodeEditor == null) {
            return;
        }
        File file = (File) getArguments().getSerializable(CompileManager.EXTRA_FILE);
        boolean result;
        if (file != null) {
            try {
                String code = getCode();
                result = mFileManager.saveFile(file, code);
                if (!result) {
                    String text = String.format("%s %s", getString(R.string.can_not_save_file), file.getName());
                    Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DLog.d(TAG, "saveFile: file is null");
        }
    }

    @Override
    public void goToLine(int line) {
        mCodeEditor.goToLine(line);
    }

    @Override
    public void formatCode() {
        String text = getCode();
        try {
            PascalCodeFormatter formatter = new PascalCodeFormatter(new StringReader(text));
            String result = formatter.getResult();
            mCodeEditor.setTextHighlighted(result);
            mCodeEditor.applyTabWidth(mCodeEditor.getText(), 0, mCodeEditor.getText().length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        if (mCodeEditor.canUndo()) {
            mCodeEditor.undo();
        } else {
            Toast.makeText(getContext(), R.string.cant_undo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void redo() {
        if (mCodeEditor.canRedo()) {
            mCodeEditor.redo();
        } else {
            Toast.makeText(getContext(), R.string.cant_redo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void paste() {
        mCodeEditor.paste();
    }

    @Override
    public void copyAll() {
        mCodeEditor.copyAll();
    }

    @NonNull
    @Override
    public String getCode() {
        return mCodeEditor.getCleanText();
    }

    @Override
    public void insert(@NonNull CharSequence text) {
        mCodeEditor.insert(text);
    }

    public EditorView getEditor() {
        return mCodeEditor;
    }

    @UiThread
    public void setLineError(@NonNull final LineNumber lineNumber) {
        mCodeEditor.setLineError(lineNumber);
        mCodeEditor.requestFocus();
        mCodeEditor.moveCursorTo(LineUtils.getIndexFromLineCol(mCodeEditor.getLayout(),
                lineNumber.getLine(), lineNumber.getColumn()));
        mCodeEditor.refresh();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScrollView != null) {
                    mScrollView.smoothScrollTo(0, LineUtils.getYAtLine(mScrollView,
                            mCodeEditor.getLineCount(), lineNumber.getLine()));
                }
            }
        }, 200);
    }

    public void refreshCodeEditor() {
        mCodeEditor.updateFromSettings();
        mCodeEditor.refresh();
    }

    public String getFilePath() {
        File file = (File) getArguments().getSerializable(CompileManager.EXTRA_FILE);
        return file.getPath();
    }

    private static class LoadCodeTask extends AsyncTask<File, Void, String> {
        private EditorView mEditorView;

        LoadCodeTask(EditorView editorView) {
            this.mEditorView = editorView;
        }

        @Override
        protected String doInBackground(File... params) {
            return FileManager.fileToString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!isCancelled()) {
                mEditorView.setText(result);
            }
        }
    }


}
