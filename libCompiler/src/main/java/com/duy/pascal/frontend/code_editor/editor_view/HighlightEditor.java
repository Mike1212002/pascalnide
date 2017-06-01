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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.duy.pascal.backend.core.PascalCompiler;
import com.duy.pascal.backend.linenumber.LineInfo;
import com.duy.pascal.backend.parse_exception.ParsingException;
import com.duy.pascal.backend.source_include.ScriptSource;
import com.duy.pascal.frontend.R;
import com.duy.pascal.frontend.code_editor.autofix.AutoFixError;
import com.duy.pascal.frontend.theme.util.CodeTheme;
import com.duy.pascal.frontend.theme.util.CodeThemeUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.duy.pascal.frontend.code_editor.completion.Patterns.BUILTIN_FUNCTIONS;
import static com.duy.pascal.frontend.code_editor.completion.Patterns.COMMENTS;
import static com.duy.pascal.frontend.code_editor.completion.Patterns.KEYWORDS;
import static com.duy.pascal.frontend.code_editor.completion.Patterns.NUMBERS;
import static com.duy.pascal.frontend.code_editor.completion.Patterns.STRINGS;
import static com.duy.pascal.frontend.code_editor.completion.Patterns.SYMBOLS;

public class HighlightEditor extends CodeSuggestsEditText
        implements View.OnKeyListener, GestureDetector.OnGestureListener {
    public static final String TAG = HighlightEditor.class.getSimpleName();
    public static final int SYNTAX_DELAY_MILLIS_SHORT = 100;
    public static final int SYNTAX_DELAY_MILLIS_LONG = 700;
    public static final int CHARS_TO_COLOR = 2500;
    private final Handler updateHandler = new Handler();
    private final Object objectThread = new Object();
    /**
     * Thread for automatically interpreting the program to catch errors.
     * Then show to edit text if there are errors
     */
    private final Runnable compileProgram = new CompileRunnable();
    public boolean showLines = true;
    public boolean wordWrap = true;
    public LineInfo lineError = null;
    protected Paint mPaintNumbers;
    protected Paint mPaintHighlight;
    protected int mPaddingDP = 4;
    protected int mPadding, mLinePadding;
    protected float mScale;
    protected int mHighlightedLine;
    protected int mHighlightStart;
    protected Rect mDrawingRect, mLineBounds;
    /**
     * the scroller instance
     */
    protected Scroller mTedScroller;
    /**
     * the velocity tracker
     */
    protected GestureDetector mGestureDetector;
    /**
     * the Max size of the view
     */
    protected Point mMaxSize;
    //Colors
    private boolean autoCompile = false;
    private CodeTheme codeTheme;
    private Context mContext;
    private boolean canEdit = true;
    @Nullable
    private ScrollView verticalScroll;
    private int lastPinLine = -1;
    private LineUtils lineUtils;
    private boolean[] isGoodLineArray;
    private int[] realLines;
    private int lineCount;
    private boolean isFind = false;
    /**
     * Disconnect this undo/redo from the text
     * view.
     */
    private boolean enabledChangeListener = false;
    /**
     * The change listener.
     */
    private EditTextChangeListener
            mChangeListener;
    private final Runnable colorRunnable_duringEditing =
            new Runnable() {
                @Override
                public void run() {
                    highlightText();
                }
            };
    private final Runnable colorRunnable_duringScroll =
            new Runnable() {
                @Override
                public void run() {
                    highlightText();
                }
            };
    private int numberWidth = 0;
    private AutoFixError mAutoFixError;

    public HighlightEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public HighlightEditor(Context context) {
        super(context);
        setup(context);
    }

    public HighlightEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    public AutoFixError getAutoFixError() {
        return mAutoFixError;
    }

    public boolean isAutoCompile() {
        return autoCompile;
    }

    public void setAutoCompile(boolean autoCompile) {
        this.autoCompile = autoCompile;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    private void setup(Context context) {
        this.mContext = context;
        mAutoFixError = new AutoFixError(this);

        lineUtils = new LineUtils();
        mPaintNumbers = new Paint();
        mPaintNumbers.setColor(getResources().getColor(R.color.color_number_color));
        mPaintNumbers.setAntiAlias(true);

        mPaintHighlight = new Paint();

        mScale = context.getResources().getDisplayMetrics().density;
        mPadding = (int) (mPaddingDP * mScale);
        mHighlightedLine = mHighlightStart = -1;
        mDrawingRect = new Rect();
        mLineBounds = new Rect();

        mGestureDetector = new GestureDetector(getContext(), HighlightEditor.this);

        updateFromSettings();

        mChangeListener = new EditTextChangeListener();

        enableTextChangedListener();
    }

    public void setColorTheme(int id) {
        codeTheme = CodeTheme.getTheme(id, mContext);
        setBackgroundColor(codeTheme.getBackground());
        setTextColor(codeTheme.getTextColor());

        int style = CodeThemeUtils.getCodeTheme(mContext, "");
        TypedArray typedArray = mContext.obtainStyledAttributes(style,
                R.styleable.CodeTheme);
        this.canEdit = typedArray.getBoolean(R.styleable.CodeTheme_can_edit, true);
        typedArray.recycle();
    }

    public void setColorTheme(String name) {
        /*
          load theme from xml
         */

        codeTheme = new CodeTheme(true);
        int style = CodeThemeUtils.getCodeTheme(mContext, name);
        TypedArray typedArray = mContext.obtainStyledAttributes(style,
                R.styleable.CodeTheme);
        typedArray.getInteger(R.styleable.CodeTheme_background_color,
                R.color.color_background_color);
        codeTheme.setErrorColor(typedArray.getInteger(R.styleable.CodeTheme_error_color,
                R.color.color_error_color));
        codeTheme.setNumberColor(typedArray.getInteger(R.styleable.CodeTheme_number_color,
                R.color.color_number_color));
        codeTheme.setKeyWordColor(typedArray.getInteger(R.styleable.CodeTheme_key_word_color,
                R.color.color_key_word_color));
        codeTheme.setCommentColor(typedArray.getInteger(R.styleable.CodeTheme_comment_color,
                R.color.color_comment_color));
        codeTheme.setStringColor(typedArray.getInteger(R.styleable.CodeTheme_string_color,
                R.color.color_string_color));
        codeTheme.setBooleanColor(typedArray.getInteger(R.styleable.CodeTheme_boolean_color,
                R.color.color_boolean_color));
        codeTheme.setOptColor(typedArray.getInteger(R.styleable.CodeTheme_opt_color,
                R.color.color_opt_color));
        setBackgroundColor(typedArray.getInteger(R.styleable.CodeTheme_background_color,
                R.color.color_background_color));
        setTextColor(typedArray.getInteger(R.styleable.CodeTheme_normal_text_color,
                R.color.color_normal_text_color));

        this.canEdit = typedArray.getBoolean(R.styleable.CodeTheme_can_edit, true);
        typedArray.recycle();
    }

    public void setLineError(@NonNull LineInfo lineError) {
        this.lineError = lineError;
    }

    public void computeScroll() {

        if (mTedScroller != null) {
            if (mTedScroller.computeScrollOffset()) {
                scrollTo(mTedScroller.getCurrX(), mTedScroller.getCurrY());
            }
        } else {
            super.computeScroll();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);
        if (mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(event);
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub

        if (isEnabled()) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this,
                    InputMethodManager.SHOW_IMPLICIT);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mEditorSetting.flingToScroll()) {
            return true;
        }

        if (mTedScroller != null) {
            mTedScroller.fling(getScrollX(), getScrollY(), -(int) velocityX, -(int) velocityY, 0, mMaxSize.x, 0,
                    mMaxSize.y);
        }
        return true;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        int lineX, baseline;
        if (lineCount != getLineCount()) {
            lineCount = getLineCount();
            lineUtils.updateHasNewLineArray(lineCount, getLayout(), getText().toString());
            isGoodLineArray = lineUtils.getGoodLines();
            realLines = lineUtils.getRealLines();
        }
        if (showLines) {
            int padding = calculateLinePadding();
            if (mLinePadding != padding) {
                mLinePadding = padding;
                setPadding(mLinePadding, mPadding, mPadding, mPadding);
            }
        }

        getDrawingRect(mDrawingRect);
        lineX = mDrawingRect.left + mLinePadding - mPadding;
        int min = 0;
        int max = lineCount;
        getLineBounds(0, mLineBounds);
        int startBottom = mLineBounds.bottom;
        int startTop = mLineBounds.top;
        getLineBounds(lineCount - 1, mLineBounds);
        int endBottom = mLineBounds.bottom;
        int endTop = mLineBounds.top;
        if (lineCount > 1 && endBottom > startBottom && endTop > startTop) {
            min = Math.max(min, ((mDrawingRect.top - startBottom) * (lineCount - 1)) / (endBottom - startBottom));
            max = Math.min(max, ((mDrawingRect.bottom - startTop) * (lineCount - 1)) / (endTop - startTop) + 1);
        }
        for (int i = min; i < max; i++) {
            baseline = getLineBounds(i, mLineBounds);

            if ((mMaxSize != null) && (mMaxSize.x < mLineBounds.right)) {
                mMaxSize.x = mLineBounds.right;
            }

            if ((i == mHighlightedLine) && (!wordWrap)) {
                canvas.drawRect(mLineBounds, mPaintHighlight);
            }
            if (showLines && isGoodLineArray[i]) {
                int realLine = realLines[i];
                canvas.drawText("" + (realLine), mDrawingRect.left, baseline, mPaintNumbers);
            }
        }
        if (showLines) {
            canvas.drawLine(lineX, mDrawingRect.top, lineX, mDrawingRect.bottom, mPaintNumbers);
        }

        getLineBounds(lineCount - 1, mLineBounds);
        if (mMaxSize != null) {
            mMaxSize.y = mLineBounds.bottom;
            mMaxSize.x = Math.max(mMaxSize.x + mPadding - mDrawingRect.width(), 0);
            mMaxSize.y = Math.max(mMaxSize.y + mPadding - mDrawingRect.height(), 0);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void updateFromSettings() {
        String name = mEditorSetting.getString(mContext.getString(R.string.key_code_theme));
        try {
            Integer id = Integer.parseInt(name);
            setColorTheme(id);
        } catch (Exception e) {
            setColorTheme(name);
        }
        setTypeface(mEditorSetting.getFont());
        setHorizontallyScrolling(!mEditorSetting.isWrapText());
        setOverScrollMode(OVER_SCROLL_ALWAYS);

        setTextSize(mEditorSetting.getTextSize());
        mPaintNumbers.setTextSize(getTextSize());

        showLines = mEditorSetting.isShowLines();

        int count = getLineCount();
        if (showLines) {
            mLinePadding = calculateLinePadding();
            setPadding(mLinePadding, mPadding, mPadding, mPadding);
        } else {
            setPadding(mPadding, mPadding, mPadding, mPadding);
        }
        autoCompile = mEditorSetting.isAutoCompile();
        wordWrap = mEditorSetting.isWrapText();
        if (wordWrap) {
            setHorizontalScrollBarEnabled(false);
        } else {
            setHorizontalScrollBarEnabled(true);
        }

        postInvalidate();
        refreshDrawableState();

        if (mEditorSetting.useImeKeyboard()) {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }  // use Fling when scrolling settings ?
        if (mEditorSetting.flingToScroll()) {
            mTedScroller = new Scroller(getContext());
            mMaxSize = new Point();
        } else {
            mTedScroller = null;
            mMaxSize = null;
        }

    }

    private int calculateLinePadding() {
        int count = getLineCount();
        int result = (int) (Math.floor(Math.log10(count)) + 1);

        Rect bounds = new Rect();
        mPaintNumbers.getTextBounds("0", 0, 1, bounds);
        numberWidth = bounds.width();
        result = (result * numberWidth) + numberWidth + mPadding;
        return result;
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    /**
     * This method used to set text and high light text
     */
    public void setTextHighlighted(CharSequence text) {
        lineError = null;
        setText(text);
        refresh();
    }

    public void refresh() {
        updateHandler.removeCallbacks(colorRunnable_duringEditing);
        updateHandler.removeCallbacks(colorRunnable_duringScroll);
        updateHandler.postDelayed(colorRunnable_duringEditing, SYNTAX_DELAY_MILLIS_SHORT);
    }

    public String getCleanText() {
        return getText().toString();
    }

    private void startCompile(int longDelay) {
        if (isAutoCompile()) {
            updateHandler.removeCallbacks(compileProgram);
            updateHandler.postDelayed(compileProgram, longDelay);
        }
    }

    /**
     * Gets the first lineInfo that is visible on the screen.
     */
    @SuppressWarnings("unused")
    public int getFirstLineIndex() {
        int scrollY;
        if (verticalScroll != null) {
            scrollY = verticalScroll.getScrollY();
        } else {
            scrollY = getScrollY();
        }
        Layout layout = getLayout();
        if (layout != null) {
            return layout.getLineForVertical(scrollY);
        }
        return -1;
    }

    /**
     * Gets the last visible lineInfo number on the screen.
     *
     * @return last lineInfo that is visible on the screen.
     */
    @SuppressWarnings("unused")
    public int getLastLineIndex() {
        int height;
        if (verticalScroll != null) {
            height = verticalScroll.getHeight();
        } else {
            height = getHeight();
        }
        int scrollY;
        if (verticalScroll != null) {
            scrollY = verticalScroll.getScrollY();
        } else {
            scrollY = getScrollY();
        }
        Layout layout = getLayout();
        if (layout != null) {
            return layout.getLineForVertical(scrollY + height);
        }
        return -1;
    }

    private void highlightLineError(Editable e) {
        try {
            //high light error lineInfo
            if (lineError != null) {
                Layout layout = getLayout();
                int line = lineError.getLine();
                int temp = line;
                while (realLines[temp] < line) temp++;
                line = temp;
                if (layout != null && line < getLineCount()) {
                    int lineStart = getLayout().getLineStart(line);
                    int lineEnd = getLayout().getLineEnd(line);
                    lineStart += lineError.getColumn();

                    //check if it contains offset from start index error to
                    //(start + offset) index
                    if (lineError.getLength() > -1) {
                        lineEnd = lineStart + lineError.getLength();
                        Log.d(TAG, "highlightLineError: " + lineError.getLength());
                    }

                    //normalize
                    lineStart = Math.max(0, lineStart);
                    lineEnd = Math.min(lineEnd, getText().length());

                    if (lineStart < lineEnd) {
                        e.setSpan(new BackgroundColorSpan(codeTheme.getErrorColor()),
                                lineStart,
                                lineEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        if (!isAutoCompile()) {
                            setSelection(lineEnd);
                        }
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void replaceAll(String what, String replace, boolean regex, boolean matchCase) {
        Pattern pattern;
        if (regex) {
            if (matchCase) {
                pattern = Pattern.compile(what);
            } else {
                pattern = Pattern.compile(what, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        } else {
            if (matchCase) {
                pattern = Pattern.compile(Pattern.quote(what));
            } else {
                pattern = Pattern.compile(Pattern.quote(what), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        }
        setText(getText().toString().replaceAll(pattern.toString(), replace));
    }

    /**
     * move cursor to lineInfo
     *
     * @param line - lineInfo in editor, start at 0
     */
    public void goToLine(int line) {
        Layout layout = getLayout();
        line = Math.min(line - 1, getLineCount() - 1);
        line = Math.max(0, line);
        if (layout != null) {
            int index = layout.getLineEnd(line);
            setSelection(index);
        }
    }

    @Override
    public void onPopupChangePosition() {
        try {
            Layout layout = getLayout();
            if (layout != null) {
                int pos = getSelectionStart();
                int line = layout.getLineForOffset(pos);
                int baseline = layout.getLineBaseline(line);
                int ascent = layout.getLineAscent(line);

                float x = layout.getPrimaryHorizontal(pos);
                float y = baseline + ascent;

                int offsetHorizontal = (int) x + mLinePadding;
                setDropDownHorizontalOffset(offsetHorizontal);

                int heightVisible = getHeightVisible();
                int offsetVertical = 0;
                if (verticalScroll != null) {
                    offsetVertical = (int) ((y + mCharHeight) - verticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) ((y + mCharHeight) - getScrollY());
                }

                int tmp = offsetVertical + getDropDownHeight() + mCharHeight;
                if (tmp < heightVisible) {
                    tmp = offsetVertical + mCharHeight / 2;
                    setDropDownVerticalOffset(tmp);
                } else {
                    tmp = offsetVertical - getDropDownHeight() - mCharHeight;
                    setDropDownVerticalOffset(tmp);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void setVerticalScroll(@Nullable ScrollView verticalScroll) {
        this.verticalScroll = verticalScroll;
    }

    /**
     * highlight find word
     *
     * @param what     - input
     * @param regex    - is java regex
     * @param wordOnly - find word only
     */
    public void find(String what, boolean regex, boolean wordOnly, boolean matchCase) {
        Pattern pattern;
        if (regex) {
            if (matchCase) {
                pattern = Pattern.compile(what);
            } else {
                pattern = Pattern.compile(what, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        } else {
            if (wordOnly) {
                if (matchCase) {
                    pattern = Pattern.compile("\\s" + what + "\\s");
                } else {
                    pattern = Pattern.compile("\\s" + Pattern.quote(what) + "\\s", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
            } else {
                if (matchCase) {
                    pattern = Pattern.compile(Pattern.quote(what));
                } else {
                    pattern = Pattern.compile(Pattern.quote(what), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
            }
        }
        Editable e = getEditableText();
        //remove all span
        BackgroundColorSpan spans[] = e.getSpans(0, e.length(), BackgroundColorSpan.class);
        for (int n = spans.length; n-- > 0; )
            e.removeSpan(spans[n]);
        //set span

        for (Matcher m = pattern.matcher(e); m.find(); ) {
            e.setSpan(new BackgroundColorSpan(codeTheme.getErrorColor()),
                    m.start(),
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void pinLine(LineInfo lineInfo) {
        if (lineInfo == null) return;
        Layout layout = getLayout();
        Editable e = getEditableText();
        if (layout != null && lineInfo.getLine() < getLineCount()) {
            try {
                if (lastPinLine < getLineCount() && lastPinLine >= 0) {
                    int lineStart = getLayout().getLineStart(lastPinLine);
                    int lineEnd = getLayout().getLineEnd(lastPinLine);
                    BackgroundColorSpan[] backgroundColorSpan = e.getSpans(lineStart, lineEnd,
                            BackgroundColorSpan.class);
                    for (BackgroundColorSpan colorSpan : backgroundColorSpan) {
                        e.removeSpan(colorSpan);
                    }
                }

                int lineStart = getLayout().getLineStart(lineInfo.getLine());
                int lineEnd = getLayout().getLineEnd(lineInfo.getLine());
                lineStart = lineStart + lineInfo.getColumn();
                if (lineStart < lineEnd) {
                    e.setSpan(new BackgroundColorSpan(codeTheme.getErrorColor()),
                            lineStart,
                            lineEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                lastPinLine = lineInfo.getLine();
            } catch (Exception ignored) {
            }
        }
    }

    public void highlightText() {
        if (isFind) return;

        disableTextChangedListener();
        highlight(getEditableText(), false);
        enableTextChangedListener();
    }

    /**
     * remove span from start to end
     */
    private void clearSpans(Editable e, int start, int end) {
        {
            ForegroundColorSpan spans[] = e.getSpans(start, end, ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                e.removeSpan(span);
            }
        }
        {
            BackgroundColorSpan spans[] = e.getSpans(start, end, BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                e.removeSpan(span);
            }
        }
        {
            StyleSpan[] spans = e.getSpans(start, end, StyleSpan.class);
            for (StyleSpan span : spans) {
                e.removeSpan(span);
            }
        }
        {
            UnderlineSpan[] spans = e.getSpans(start, end, UnderlineSpan.class);
            for (UnderlineSpan span : spans) {
                e.removeSpan(span);
            }
        }
    }

    public Editable highlight(Editable editable, boolean newText) {
//        editable.clearSpans();
        if (editable.length() == 0) {
            return editable;
        }

        int editorHeight = getHeightVisible();

        int firstVisibleIndex;
        int lastVisibleIndex;
        if (!newText && editorHeight > 0) {
            if (verticalScroll != null && getLayout() != null) {
                firstVisibleIndex = getLayout().getLineStart(getFirstLineIndex());
            } else {
                firstVisibleIndex = 0;
            }
            if (verticalScroll != null && getLayout() != null) {
                lastVisibleIndex = getLayout().getLineStart(getLastLineIndex());
            } else {
                lastVisibleIndex = getText().length();
            }
        } else {
            firstVisibleIndex = 0;
            lastVisibleIndex = CHARS_TO_COLOR;
        }
        int delta = (lastVisibleIndex - firstVisibleIndex) / 5;
        firstVisibleIndex -= delta;
        lastVisibleIndex += delta;

        // normalize
        if (firstVisibleIndex < 0)
            firstVisibleIndex = 0;
        if (lastVisibleIndex > editable.length())
            lastVisibleIndex = editable.length();
        if (firstVisibleIndex > lastVisibleIndex)
            firstVisibleIndex = lastVisibleIndex;

        //clear all span for firstVisibleIndex to lastVisibleIndex
        clearSpans(editable, firstVisibleIndex, lastVisibleIndex);

        CharSequence textToHighlight = editable.subSequence(firstVisibleIndex, lastVisibleIndex);
        color(editable, textToHighlight, firstVisibleIndex);
        applyTabWidth(editable, firstVisibleIndex, lastVisibleIndex);
        return editable;
    }

    private void color(Editable allText, CharSequence textToHighlight, int start) {
        try {
            //high light number
            for (Matcher m = NUMBERS.matcher(textToHighlight); m.find(); ) {
                allText.setSpan(new ForegroundColorSpan(codeTheme.getNumberColor()),
                        start + m.start(),
                        start + m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (Matcher m = KEYWORDS.matcher(textToHighlight); m.find(); ) {
                allText.setSpan(new ForegroundColorSpan(codeTheme.getKeywordColor()),
                        start + m.start(),
                        start + m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (Matcher m = BUILTIN_FUNCTIONS.matcher(textToHighlight); m.find(); ) {
                allText.setSpan(new ForegroundColorSpan(codeTheme.getKeywordColor()),
                        start + m.start(),
                        start + m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //find it
            for (Matcher m = SYMBOLS.matcher(textToHighlight); m.find(); ) {
                //if match, you can replace text with other style
                allText.setSpan(new ForegroundColorSpan(codeTheme.getOptColor()),
                        start + m.start(),
                        start + m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
            for (Matcher m = STRINGS.matcher(textToHighlight); m.find(); ) {
                ForegroundColorSpan spans[] = allText.getSpans(start + m.start(), start + m.end(),
                        ForegroundColorSpan.class);

                for (int n = spans.length; n-- > 0; )
                    allText.removeSpan(spans[n]);

                allText.setSpan(new ForegroundColorSpan(codeTheme.getStringColor()),
                        start + m.start(),
                        start + m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (Matcher m = COMMENTS.matcher(textToHighlight); m.find(); ) {
                ForegroundColorSpan spans[] = allText.getSpans(start + m.start(), start + m.end(),
                        ForegroundColorSpan.class);
                for (int n = spans.length; n-- > 0; )
                    allText.removeSpan(spans[n]);

                allText.setSpan(new ForegroundColorSpan(codeTheme.getCommentColor()),
                        start + m.start(),
                        start + m.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            highlightLineError(allText);
        } catch (Exception e) {
        }
    }

    public void enableTextChangedListener() {
        if (!enabledChangeListener) {
            addTextChangedListener(mChangeListener);
            enabledChangeListener = true;
        }
    }

    public void disableTextChangedListener() {
        enabledChangeListener = false;
        removeTextChangedListener(mChangeListener);
    }

    public void updateTextHighlight() {
        if (hasSelection() || updateHandler == null)
            return;
        updateHandler.removeCallbacks(colorRunnable_duringEditing);
        updateHandler.removeCallbacks(colorRunnable_duringScroll);
        updateHandler.postDelayed(colorRunnable_duringEditing, SYNTAX_DELAY_MILLIS_LONG);
    }

    public void showKeyboard() {
        requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Class that listens to changes in the text.
     */
    private final class EditTextChangeListener
            implements TextWatcher {
        private int start;
        private int count;

        public void beforeTextChanged(
                CharSequence s, int start, int count,
                int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before,
                                  int count) {
            this.start = start;
            this.count = count;
            isFind = false;
        }

        public void afterTextChanged(Editable s) {
            updateTextHighlight();

            if (!autoCompile) {
                lineError = null;
            }
            startCompile(200);
            if (s.length() > start && count == 1) {
                char textToInsert = getCloseBracket(s.charAt(start), start);
                if (textToInsert != 0) {
                    try {
                        s.insert(start + 1, Character.toString(textToInsert));
                        setSelection(start);
                    } catch (Exception ignored) {
                    }
                }
            }

        }
    }

    private class CompileRunnable implements Runnable {
        @Override
        public void run() {
            try {
                PascalCompiler.loadPascal("temp", new StringReader(getCleanText()),
                        new ArrayList<ScriptSource>(), null);
                lineError = null;
            } catch (ParsingException e) {
                if (e.getLineInfo() != null) {
                    synchronized (objectThread) {
                        lineError = e.getLineInfo();
                    }
                }
                e.printStackTrace();
            } catch (Exception ignored) {
            }
        }


    }
}
