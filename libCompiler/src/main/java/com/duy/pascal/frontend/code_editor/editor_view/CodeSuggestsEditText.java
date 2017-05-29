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

package com.duy.pascal.frontend.code_editor.editor_view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.MultiAutoCompleteTextView;

import com.duy.pascal.frontend.DLog;
import com.duy.pascal.frontend.EditorSetting;
import com.duy.pascal.frontend.R;
import com.duy.pascal.frontend.code_editor.completion.KeyWord;
import com.duy.pascal.frontend.code_editor.editor_view.adapters.CodeSuggestAdapter;
import com.duy.pascal.frontend.code_editor.editor_view.adapters.InfoItem;
import com.duy.pascal.frontend.structure.viewholder.StructureType;

import java.util.ArrayList;

/**
 * AutoSuggestsEditText
 * show hint when typing
 * Created by Duy on 28-Feb-17.
 */

public abstract class CodeSuggestsEditText extends AutoIndentEditText {
    private static final String TAG = CodeSuggestsEditText.class.getName();

    public int mCharHeight = 0;
    protected EditorSetting mEditorSetting;
    protected SymbolsTokenizer mTokenizer;
    private CodeSuggestAdapter mAdapter;
    private boolean enoughToFilter = false;


    public CodeSuggestsEditText(Context context) {
        super(context);
        init();
    }

    public CodeSuggestsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeSuggestsEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * slipt string in edittext and put it to list keyword
     */
    public void setDefaultKeyword() {
        ArrayList<InfoItem> data = new ArrayList<>();
        for (String s : KeyWord.ALL_KEY_WORD) {
            data.add(new InfoItem(StructureType.TYPE_KEY_WORD, s));
        }
        setSuggestData(data);
    }

    private void init() {
        mEditorSetting = new EditorSetting(getContext());
        setDefaultKeyword();
        mTokenizer = new SymbolsTokenizer();
        setTokenizer(mTokenizer);
        setThreshold(1);
        invalidateCharHeight();
    }

    /**
     * @return the height of view display on screen
     */
    public int getHeightVisible() {
        Rect r = new Rect();
        // r will be populated with the coordinates of     your view
        // that area still visible.
        getWindowVisibleDisplayFrame(r);
        return r.bottom - r.top;
    }

    private void invalidateCharHeight() {
        mCharHeight = (int) Math.ceil(getPaint().getFontSpacing());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        onPopupChangePosition();
    }

    public abstract void onPopupChangePosition();

    /**
     * invalidate data for auto suggest
     */
    public void setSuggestData(String[] data) {
        ArrayList<InfoItem> items = new ArrayList<>();
        for (String s : data) {
            items.add(new InfoItem(StructureType.TYPE_KEY_WORD, s));
        }
        setSuggestData(items);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (DLog.DEBUG) {
            DLog.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" +
                    oldw + "], oldh = [" + oldh + "]");
        }
        onDropdownChangeSize();
    }

    /**
     * this method will be change size of popup window
     */
    protected void onDropdownChangeSize() {
        // 1/2 width of screen
        int width = getWidth() / 2;
        setDropDownWidth(width);

        // 0.5 height of screen
        int height = getHeightVisible() / 2;
        setDropDownHeight(height);

        //change position
        onPopupChangePosition();
    }

    @Override
    public void showDropDown() {
        if (!isPopupShowing()) {
            if (hasFocus()) {
                super.showDropDown();
            }
        }
    }

    public void setEnoughToFilter(boolean enoughToFilter) {
        this.enoughToFilter = enoughToFilter;
    }

    @Override
    public boolean enoughToFilter() {
        if (enoughToFilter) {
            return true;
        }
        return super.enoughToFilter();
    }

    public void restoreAfterClick(final String[] data) {
        final ArrayList<InfoItem> suggestData = (ArrayList<InfoItem>) getSuggestData().clone();
        setSuggestData(data);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setEnoughToFilter(true);
                showDropDown();
                setEnoughToFilter(false);

                //when user click item, restore data
                setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        setSuggestData(suggestData);

                        //don't handle twice
                        setOnItemClickListener(null);
                    }
                });

            }
        }, 50);
    }

    public ArrayList<InfoItem> getSuggestData() {
        return mAdapter.getAllItems();
    }

    /**
     * invalidate data for auto suggest
     */
    public void setSuggestData(ArrayList<InfoItem> data) {
        DLog.d(TAG, "setSuggestData: ");
            mAdapter = new CodeSuggestAdapter(getContext(), R.layout.list_item_suggest, data);

        setAdapter(mAdapter);
        onDropdownChangeSize();
    }

    public void addKeywords(String[] allKeyWord) {
        ArrayList<InfoItem> newData = new ArrayList<>();
        for (String s : allKeyWord) {
            newData.add(new InfoItem(StructureType.TYPE_KEY_WORD, s));
        }
        ArrayList<InfoItem> oldItems = mAdapter.getAllItems();
        oldItems.addAll(newData);
        setSuggestData(oldItems);
    }

    private class SymbolsTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        String token = "!@#$%^&*()_+-={}|[]:;'<>/<.? \n\t";

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && !token.contains(Character.toString(text.charAt(i - 1)))) {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            while (i < len) {
                if (token.contains(Character.toString(text.charAt(i - 1)))) {
                    return i;
                } else {
                    i++;
                }
            }
            return len;
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }
            return text;
        }
    }
}

