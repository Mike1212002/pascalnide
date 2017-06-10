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

package com.duy.pascal.frontend.view.exec_screen.console;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;

import com.duy.pascal.backend.builtin_libraries.graph.GraphScreen;
import com.duy.pascal.backend.builtin_libraries.graph.model.GraphObject;
import com.duy.pascal.frontend.DLog;
import com.duy.pascal.frontend.setting.PascalPreferences;

import static com.duy.pascal.frontend.utils.StringCompare.isGreaterEqual;
import static com.duy.pascal.frontend.utils.StringCompare.isLessThan;

public class ConsoleView extends View implements
        GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
    public static final String THE_DELETE_COMMAND = "\u2764";
    public static final String THE_ENTER_KEY = "\u2713";
    private static final String TAG = "ConsoleView";

    static {
        DLog.TAG = TAG;
    }

    public Handler handler = new Handler();
    public int firstLine;
    private boolean graphMode = false;
    private GraphScreen mGraphScreen;
    //    text style, size of console
    private TextRenderer mTextRenderer;
    //      store screen size and dimen
    private ConsoleScreen mConsoleScreen;
    //     Cursor of console
    private ConsoleCursor mCursor;
    private Context mContext;
    //      Data of console
    private ScreenBuffer mScreenBufferData = new ScreenBuffer();
    private Rect visibleRect = new Rect();
    private Runnable checkSize = new Runnable() {
        public void run() {
            if (updateSize()) {
                invalidate();
            }
            handler.postDelayed(this, 1000);
        }
    };
    private Runnable blink = new Runnable() {
        public void run() {
            mCursor.toggleState();
            invalidate();
            handler.postDelayed(this, 1000);
        }
    };
    private float mScrollRemainder;
    private GestureDetectorCompat mGestureDetector;
    private boolean filterKey = false;
    private PascalPreferences mPascalPreferences;
    private String mImeBuffer = "";
    private TextConsole[] textImeBuffer;

    public ConsoleView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public ConsoleView(Context context, AttributeSet attrs, int defStyles) {
        super(context, attrs, defStyles);
        init(context);

    }

    public ConsoleView(Context context) {
        super(context);
        init(context);
    }

    public TextRenderer getTextRenderer() {
        return mTextRenderer;
    }

    public ConsoleCursor getCursorConsole() {
        return mCursor;
    }

    public boolean isGraphMode() {
        return graphMode;
    }

    public void setGraphMode(boolean graphMode) {
        this.graphMode = graphMode;
    }

    private void init(Context context) {
        mContext = context;
        mGestureDetector = new GestureDetectorCompat(getContext(), this);
        mGestureDetector.setOnDoubleTapListener(this);
        mPascalPreferences = new PascalPreferences(context);

        mGraphScreen = new GraphScreen(context);
        mConsoleScreen = new ConsoleScreen(mPascalPreferences);
        mConsoleScreen.setBackgroundColor(Color.BLACK);

        mTextRenderer = new TextRenderer(getTextSize(TypedValue.COMPLEX_UNIT_SP,
                mPascalPreferences.getConsoleFontSize()));
        mTextRenderer.setTextColor(Color.WHITE);

        firstLine = 0;
        mScreenBufferData.firstIndex = 0;
        mScreenBufferData.textConsole = null;

        mCursor = new ConsoleCursor(0, 0, Color.DKGRAY);
        mCursor.setCoordinate(0, 0);
        mCursor.setCursorBlink(true);
        mCursor.setVisible(true);
    }


    public void putString(String c) {
        mScreenBufferData.textBuffer.writeBuffer(c);
    }

    public String readString() {
        return mScreenBufferData.textBuffer.readBuffer();
    }

    /**
     * @return one key in the buffer key
     */
    public char readKey() {
        return mScreenBufferData.keyBuffer.getChar();
    }

    public void write(String c, boolean isMaskBuffer) {
        int index = mScreenBufferData.firstIndex + mCursor.y * mConsoleScreen.consoleColumn + mCursor.x;
        if (index >= mConsoleScreen.getScreenSize()) {
            index -= mConsoleScreen.getScreenSize();
        }
        switch (c) {
            case "\n":
                mScreenBufferData.textConsole[index].setText("\n");
                mScreenBufferData.textConsole[index].setTextBackground(mTextRenderer.getBackgroundColor());
                mScreenBufferData.textConsole[index].setTextColor(mTextRenderer.getTextColor());
                mScreenBufferData.textConsole[index].setAlpha(mTextRenderer.getAlpha());
                nextLine();
                break;
            case "\177":
            case THE_DELETE_COMMAND:
                backspace(index);
                break;
            default:
                makeCursorVisible();
                if (isGreaterEqual(c, " ")) {
                    mScreenBufferData.textConsole[index].setText(c);
                    mScreenBufferData.textConsole[index].setTextBackground(
                            isMaskBuffer ? Color.DKGRAY : mTextRenderer.getBackgroundColor());
                    mScreenBufferData.textConsole[index].setTextColor(mTextRenderer.getTextColor());
                    mScreenBufferData.textConsole[index].setAlpha(mTextRenderer.getAlpha());
                    mCursor.x++;
                    if (mCursor.x >= mConsoleScreen.consoleColumn) {
                        nextLine();
                    }
                }
        }

        postInvalidate();
    }

    //set cursor index
    public void setConsoleCursorPosition(int x, int y) {
        int index, i;
        mCursor.y = y;
        index = mScreenBufferData.firstIndex + mCursor.y * mConsoleScreen.consoleColumn;
        if (index >= mConsoleScreen.getScreenSize()) index -= mConsoleScreen.getScreenSize();
        i = index;

        while (i - index <= x) {
            if (isLessThan(mScreenBufferData.textConsole[i].getSingleString(), " ")) break;
            i++;
        }

        while (i - index < x) {
            if (isLessThan(mScreenBufferData.textConsole[i].getSingleString(), " ")) {
                mScreenBufferData.textConsole[i].setText(" ");
            }
            i++;
        }
        mCursor.x = x;
    }

    public void backspace(int index) {
        if (mCursor.x > 0) {
            mCursor.x--;
            mScreenBufferData.textConsole[index - 1].setText("\0");
        } else {
            if (mCursor.y > 0) {
                if (isGreaterEqual(mScreenBufferData.textConsole[index - 1].getSingleString(), " ")) {
                    mScreenBufferData.textConsole[index - 1].setText("\0");
                    mCursor.x = mConsoleScreen.consoleColumn - 1;
                    mCursor.y--;
                    makeCursorVisible();
                }
            }
        }
    }

    public synchronized void writeString(String msg) {
        for (int i = 0; i < msg.length(); i++)
            write(msg.substring(i, i + 1), false);
    }

    private void nextLine() {
        mCursor.x = 0;
        mCursor.y++;
        if (mCursor.y >= mConsoleScreen.getMaxLines()) {
            mCursor.y = mConsoleScreen.getMaxLines() - 1;
            for (int i = 0; i < mConsoleScreen.consoleColumn; i++)
                mScreenBufferData.textConsole[mScreenBufferData.firstIndex + i].setText("\0");
            mScreenBufferData.firstIndex += mConsoleScreen.consoleColumn;
            if (mScreenBufferData.firstIndex >= mConsoleScreen.getScreenSize())
                mScreenBufferData.firstIndex = 0;
        }
        makeCursorVisible();
    }

    public void showPrompt() {
        writeString("Initialize the console screen..." + "\n");
        writeString("Size: " + mConsoleScreen.consoleRow + "x" + mConsoleScreen.consoleColumn + "\n");
        writeString("---------------------------" + "\n");
    }

    public float getTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;
        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();
        return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }

    /**
     * clear screen
     * clrscr command in pascal
     */
    public void clearScreen() {
        for (int i = 0; i < mConsoleScreen.getScreenSize(); i++)
            mScreenBufferData.textConsole[i].setText("\0");
        mCursor.setCoordinate(0, 0);
        firstLine = 0;
        mScreenBufferData.firstIndex = 0;
        mConsoleScreen.setBackgroundColor(mTextRenderer.getBackgroundColor());
        postInvalidate();
    }

    //  Update text size
    public boolean updateSize() {
//        Log.d(TAG, "updateSize() called");

        boolean invalid = false;

        getWindowVisibleDisplayFrame(visibleRect);
        int newHeight;
        int newWidth;
        int newTop;
        int newLeft;
//        Log.d(TAG, "updateSize: " + visibleRect + " top = " + getTop());
        if (mConsoleScreen.isFullScreen()) {
            newTop = Math.min(getTop(), visibleRect.top);
            newHeight = visibleRect.bottom - newTop;
        } else {
            newTop = getTop();
            newHeight = visibleRect.height();
        }
        newWidth = visibleRect.width();
        newLeft = visibleRect.left;

        if ((newWidth != mConsoleScreen.getVisibleWidth())
                || (newHeight != mConsoleScreen.getVisibleHeight())) {
            mConsoleScreen.setVisibleWidth(newWidth);
            mConsoleScreen.setVisibleHeight(newHeight);
            try {
                updateSize(mConsoleScreen.getVisibleWidth(), mConsoleScreen.getVisibleHeight());
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            invalid = true;
        }
        if ((newLeft != mConsoleScreen.getLeftVisible())
                || (newTop != mConsoleScreen.getTopVisible())) {
            mConsoleScreen.setLeftVisible(newLeft);
            mConsoleScreen.setTopVisible(newTop);
            invalid = true;
        }

        if (invalid) postInvalidate();
        return invalid;
    }

    public void makeCursorVisible() {
        if (mCursor.y - firstLine >= mConsoleScreen.consoleRow) {
            firstLine = mCursor.y - mConsoleScreen.consoleRow + 1;
        } else if (mCursor.y < firstLine) {
            firstLine = mCursor.y;
        }
    }

    private int trueIndex(int i, int first, int max) {
        i += first;
        if (i > max) i -= max;
        return i;
    }

    public boolean updateSize(@IntRange(from = 1) int newWidth,
                              @IntRange(from = 1) int newHeight) throws ArrayIndexOutOfBoundsException {
//        Log.d(TAG, "updateSize() called with: newWidth = [" + newWidth + "], newHeight = [" + newHeight + "]");

        int newColumn = (int) (newWidth / mTextRenderer.getCharWidth());
        int i, j;
        int newFirstIndex = 0;
        int newRow = newHeight / mTextRenderer.getCharHeight();
        boolean value = newColumn != mConsoleScreen.consoleColumn || newRow != mConsoleScreen.consoleColumn;
        mConsoleScreen.consoleRow = newRow;
        if (newColumn != mConsoleScreen.consoleColumn) {
            int newScreenSize = mConsoleScreen.getMaxLines() * newColumn;
//            DLog.d(TAG, "updateSize: " + newScreenSize + " " + mConsoleScreen.getMaxLines() + " " + newColumn);
            TextConsole newScreenBuffer[] = new TextConsole[newScreenSize];
            for (i = 0; i < newScreenSize; i++) {
                newScreenBuffer[i] = new TextConsole();
            }
            if (mScreenBufferData.textConsole != null) {
                i = 0;
                int nextj = 0;
                int endi = mCursor.y * mConsoleScreen.consoleColumn + mCursor.x;
                String c;
                do {
                    j = nextj;
                    do {
                        c = mScreenBufferData.textConsole[trueIndex(i++, mScreenBufferData.firstIndex, mConsoleScreen.getScreenSize())].getSingleString();
                        newScreenBuffer[trueIndex(j++, newFirstIndex, newScreenSize)].setText(c);
                        newFirstIndex = Math.max(0, j / newColumn - mConsoleScreen.getMaxLines() + 1) * newColumn;
                    }
                    while (isGreaterEqual(c, " "));
                    i--;
                    j--;

                    i += (mConsoleScreen.consoleColumn - i % mConsoleScreen.consoleColumn);
                    nextj = j + (newColumn - j % newColumn);
                }
                while (i < endi);
                if (c.equals("\n")) j = nextj;
                mCursor.y = j / newColumn;
                mCursor.x = j % newColumn;
            }
            mConsoleScreen.setConsoleColumn(newColumn);
            mConsoleScreen.setScreenSize(newScreenSize);
            mScreenBufferData.setTextConsole(newScreenBuffer);
            mScreenBufferData.firstIndex = newFirstIndex;
        }
        makeCursorVisible();
        return value;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_NULL;
//        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new InputConnection() {
            /**
             * Used to handle composing text requests
             */
            private int mCursor;
            private int mComposingTextStart;
            private int mComposingTextEnd;
            private int mSelectedTextStart = 0;
            private int mSelectedTextEnd = 0;
            private boolean mInBatchEdit;

            private void sendText(CharSequence text) {
                DLog.d(TAG, "sendText: " + text);
                int n = text.length();
                for (int i = 0; i < n; i++) {
                    mScreenBufferData.keyBuffer.putChar(text.charAt(i));
                    putString(Character.toString(text.charAt(i)));
                }
            }


            @Override
            public boolean performEditorAction(int actionCode) {
                DLog.d(TAG, "performEditorAction: " + actionCode);
                if (actionCode == EditorInfo.IME_ACTION_DONE
                        || actionCode == EditorInfo.IME_ACTION_GO
                        || actionCode == EditorInfo.IME_ACTION_NEXT
                        || actionCode == EditorInfo.IME_ACTION_SEND
                        || actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    sendText("\n");
                    return true;
                }
                return false;
            }


            public boolean beginBatchEdit() {
                {
                    DLog.w(TAG, "beginBatchEdit");
                }
                setImeBuffer("");
                mCursor = 0;
                mComposingTextStart = 0;
                mComposingTextEnd = 0;
                mInBatchEdit = true;
                return true;
            }

            public boolean clearMetaKeyStates(int arg0) {
                {
                    DLog.w(TAG, "clearMetaKeyStates " + arg0);
                }
                return false;
            }

            public boolean commitCompletion(CompletionInfo arg0) {
                {
                    DLog.w(TAG, "commitCompletion " + arg0);
                }
                return false;
            }

            @Override
            public boolean commitCorrection(CorrectionInfo correctionInfo) {
                return false;
            }

            public boolean endBatchEdit() {
                {
                    DLog.w(TAG, "endBatchEdit");
                }
                mInBatchEdit = false;
                return true;
            }

            public boolean finishComposingText() {
                {
                    DLog.w(TAG, "finishComposingText");
                }
                sendText(mImeBuffer);
                setImeBuffer("");
                mComposingTextStart = 0;
                mComposingTextEnd = 0;
                mCursor = 0;
                return true;
            }

            public int getCursorCapsMode(int arg0) {
                {
                    DLog.w(TAG, "getCursorCapsMode(" + arg0 + ")");
                }
                return 0;
            }

            public ExtractedText getExtractedText(ExtractedTextRequest arg0,
                                                  int arg1) {
                {
                    DLog.w(TAG, "getExtractedText" + arg0 + "," + arg1);
                }
                return null;
            }

            public CharSequence getTextAfterCursor(int n, int flags) {
                {
                    DLog.w(TAG, "getTextAfterCursor(" + n + "," + flags + ")");
                }
                int len = Math.min(n, mImeBuffer.length() - mCursor);
                if (len <= 0 || mCursor < 0 || mCursor >= mImeBuffer.length()) {
                    return "";
                }
                return mImeBuffer.substring(mCursor, mCursor + len);
            }

            public CharSequence getTextBeforeCursor(int n, int flags) {
                {
                    DLog.w(TAG, "getTextBeforeCursor(" + n + "," + flags + ")");
                }
                int len = Math.min(n, mCursor);
                if (len <= 0 || mCursor < 0 || mCursor >= mImeBuffer.length()) {
                    return "";
                }
                return mImeBuffer.substring(mCursor - len, mCursor);
            }

            public boolean performContextMenuAction(int arg0) {
                {
                    DLog.w(TAG, "performContextMenuAction" + arg0);
                }
                return true;
            }

            public boolean performPrivateCommand(String arg0, Bundle arg1) {
                {
                    DLog.w(TAG, "performPrivateCommand" + arg0 + "," + arg1);
                }
                return true;
            }

            @Override
            public boolean requestCursorUpdates(int cursorUpdateMode) {
                return false;
            }

            @Override
            public Handler getHandler() {
                return null;
            }

            @Override
            public void closeConnection() {

            }

            @Override
            public boolean commitContent(@NonNull InputContentInfo inputContentInfo, int flags, Bundle opts) {
                return false;
            }

            public boolean reportFullscreenMode(boolean arg0) {
                {
                    DLog.w(TAG, "reportFullscreenMode" + arg0);
                }
                return true;
            }


            public boolean commitText(CharSequence text, int newCursorPosition) {
                {
                    DLog.w(TAG, "commitText(\"" + text + "\", " + newCursorPosition + ")");
                }
                char[] characters = text.toString().toCharArray();
                for (char character : characters) {
                    mScreenBufferData.keyBuffer.putChar(character);
                }
                clearComposingText();
                sendText(text);
                setImeBuffer("");
                mCursor = 0;
                return true;
            }

            private void clearComposingText() {
                setImeBuffer(mImeBuffer.substring(0, mComposingTextStart) +
                        mImeBuffer.substring(mComposingTextEnd));
                if (mCursor < mComposingTextStart) {
                    // do nothing
                } else if (mCursor < mComposingTextEnd) {
                    mCursor = mComposingTextStart;
                } else {
                    mCursor -= mComposingTextEnd - mComposingTextStart;
                }
                mComposingTextEnd = mComposingTextStart = 0;
            }

            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                {
                    DLog.w(TAG, "deleteSurroundingText(" + leftLength +
                            "," + rightLength + ")");
                }
                if (leftLength > 0) {
                    for (int i = 0; i < leftLength; i++) {
                        sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    }
                } else if ((leftLength == 0) && (rightLength == 0)) {
                    // Delete key held down / repeating
                    sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }
                // TODO: handle forward deletes.
                return true;
            }

            @Override
            public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
                return false;
            }


            public boolean sendKeyEvent(KeyEvent event) {
                {
                    DLog.w(TAG, "sendKeyEvent(" + event + ")");
                }
                // Some keys are sent here rather than to commitText.
                // In particular, del and the digit keys are sent here.
                // (And I have reports that the HTC Magic also sends Return here.)
                // As a bit of defensive programming, handle every key.
                dispatchKeyEvent(event);
                return true;
            }

            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                {
                    DLog.w(TAG, "setComposingText(\"" + text + "\", " + newCursorPosition + ")");
                }

                setImeBuffer(mImeBuffer.substring(0, mComposingTextStart) +
                        text + mImeBuffer.substring(mComposingTextEnd));
                mComposingTextEnd = mComposingTextStart + text.length();
                mCursor = newCursorPosition > 0 ? mComposingTextEnd + newCursorPosition - 1
                        : mComposingTextStart - newCursorPosition;
                return true;
            }

            public boolean setSelection(int start, int end) {
                {
                    DLog.w(TAG, "setSelection" + start + "," + end);
                }
                int length = mImeBuffer.length();
                if (start == end && start > 0 && start < length) {
                    mSelectedTextStart = mSelectedTextEnd = 0;
                    mCursor = start;
                } else if (start < end && start > 0 && end < length) {
                    mSelectedTextStart = start;
                    mSelectedTextEnd = end;
                    mCursor = start;
                }
                return true;
            }

            public boolean setComposingRegion(int start, int end) {
                {
                    DLog.w(TAG, "setComposingRegion " + start + "," + end);
                }
                if (start < end && start > 0 && end < mImeBuffer.length()) {
                    clearComposingText();
                    mComposingTextStart = start;
                    mComposingTextEnd = end;
                }
                return true;
            }

            public CharSequence getSelectedText(int flags) {
                try {

                    {
                        DLog.w(TAG, "getSelectedText " + flags);
                    }

                    if (mImeBuffer.length() < 1) {
                        return "";
                    }

                    return mImeBuffer.substring(mSelectedTextStart, mSelectedTextEnd + 1);

                } catch (Exception ignored) {

                }

                return "";
            }

        };
    }

    private void setImeBuffer(String buffer) {
        DLog.d(TAG, "setImeBuffer: " + buffer);
        //delete last buffer in screen
        for (int i = 0; i < mImeBuffer.length(); i++) {
            write(THE_DELETE_COMMAND, false);
        }
        mImeBuffer = buffer;
        if (mImeBuffer.isEmpty()) return;
        for (int i = 0; i < mImeBuffer.length(); i++)
            write(mImeBuffer.substring(i, i + 1), true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DLog.d(TAG, "onKeyDown: " + event);
        if (event.isSystem()) {
            return super.onKeyDown(keyCode, event);
        }
        mScreenBufferData.keyBuffer.putChar((char) event.getUnicodeChar()); //scan code

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            putString(THE_DELETE_COMMAND);
            return true;
        }
        String c = event.getCharacters();
        if (c == null) {
            c = Character.valueOf((char) event.getUnicodeChar()).toString();
        }
        putString(c);
        return true;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        DLog.d(TAG, "onKeyUp: " + event);
        if (event.isSystem()) {
            return super.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        DLog.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h +
                "], oldw = [" + oldw + "], oldh = [" + oldh + "]");

        mGraphScreen.onSizeChange(w, h);
        boolean b = updateSize();
    }

    public void drawText(Canvas canvas, int left, int top) {
        int index = mScreenBufferData.firstIndex + firstLine * mConsoleScreen.consoleColumn;
        if (index >= mConsoleScreen.getScreenSize()) {
            index -= mConsoleScreen.getScreenSize();
        }
        top -= mTextRenderer.getCharAscent();

        //draw cursor
        mCursor.drawCursor(canvas,
                left + mCursor.x * mTextRenderer.getCharWidth(),
                top + (mCursor.y - firstLine) * mTextRenderer.getCharHeight(),
                mTextRenderer.getCharHeight(), mTextRenderer.getCharWidth(),
                mTextRenderer.getCharDescent());

        int count;
        for (int row = 0; row < mConsoleScreen.consoleRow; row++) {
            if (row > mCursor.y - firstLine) break;
            count = 0;
            while ((count < mConsoleScreen.consoleColumn) &&
                    isGreaterEqual(mScreenBufferData
                            .textConsole[count + index].getSingleString(), " ")) {
                count++;
            }

            mTextRenderer.draw(canvas, left, top, mScreenBufferData.textConsole, index, count);

            top += mTextRenderer.getCharHeight();
            index += mConsoleScreen.consoleColumn;

            if (index >= mConsoleScreen.getScreenSize()) index = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int w = getWidth();
        int h = getHeight();
        if (graphMode) {
            // draw bitmap graph
            if (!mGraphScreen.getGraphBitmap().isRecycled())
                canvas.drawBitmap(mGraphScreen.getGraphBitmap(), 0, 0, mGraphScreen.getBackgroundPaint());
        } else {
            Log.d(TAG, "onDraw: " + mConsoleScreen.getTopVisible());
            mConsoleScreen.drawBackground(canvas, mConsoleScreen.getLeftVisible(),
                    mConsoleScreen.getTopVisible(), w, h);
            drawText(canvas, mConsoleScreen.getLeftVisible(), mConsoleScreen.getTopVisible());
        }
    }

    /**
     * clear data
     */
    public void onDestroy() {
        mGraphScreen.gc();
        mConsoleScreen.clearAll();
        mScreenBufferData.clearAll();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        distanceY += mScrollRemainder;
        int deltaRows = (int) (distanceY / mTextRenderer.getCharHeight());

        mScrollRemainder = distanceY - deltaRows * mTextRenderer.getCharHeight();

        firstLine = Math.max(0, Math.min(firstLine + deltaRows, mCursor.y));

        invalidate();
        return true;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO: add animation man's (non animated) fling
        mScrollRemainder = 0.0f;
        onScroll(e1, e2,/* 2 * */velocityX, -/*2 **/ velocityY * 0.1f);
        return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
//        doShowSoftKeyboard();
        return true;
    }


    public boolean onDown(MotionEvent e) {
        mScrollRemainder = 0.0f;
        return true;
    }

    public void onLongPress(MotionEvent e) {
        showContextMenu();
    }

    public void onPause() {
        handler.removeCallbacks(checkSize);
        handler.removeCallbacks(blink);
    }

    public void onResume() {
        handler.postDelayed(checkSize, 1000);
        handler.postDelayed(blink, 1000);
        updateSize();
    }


    private void doShowSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
    }

    public GraphScreen getGraphScreen() {
        return mGraphScreen;
    }

    public boolean isFilterKey() {
        return filterKey;
    }

    public void setFilterKey(boolean filterKey) {
        this.filterKey = filterKey;
    }

    //pascal
    public void setConsoleTextColor(int textColor) {
        DLog.d(TAG, "setConsoleTextColor: " + textColor);
        mTextRenderer.setTextColor(textColor);
    }

    //pascal
    public void setConsoleTextBackground(int color) {
        mTextRenderer.setTextBackgroundColor(color);
    }


    // move cursor to (x, y)
    public void gotoXY(int x, int y) {
        if (x <= 0) x = 1;
        else if (x > mConsoleScreen.consoleColumn) x = mConsoleScreen.consoleColumn;
        if (y <= 0) y = 1;
        else if (y > mConsoleScreen.getMaxLines()) y = mConsoleScreen.getMaxLines();
        setConsoleCursorPosition(x - 1, y - 1);
        makeCursorVisible();
        postInvalidate();
    }

    // `return x coordinate of cursor in console
    public int whereX() {
        return mCursor.x + 1;
    }

    /**
     * return y coordinate of cursor in console*
     */
    public int whereY() {
        return mCursor.y + 1;
    }


    public boolean isKeyPressed() {
        return (mScreenBufferData.keyBuffer.rear > mScreenBufferData.keyBuffer.front);
    }

    //pascal
    public int getColorPixel(int x, int y) {
        return mGraphScreen.getColorPixel(x, y);
    }

    //pascal
    public void addGraphObject(GraphObject graphObject) {
        mGraphScreen.addGraphObject(graphObject);
        postInvalidate();
    }

    //pascal
    public int getXCursorPixel() {
        return mGraphScreen.getXCursor();
    }

    //pascal
    public int getYCursorPixel() {
        return mGraphScreen.getYCursor();
    }

    /**
     * mode graph
     *
     * @return Return current drawing color
     */
    public int getForegroundGraphColor() {
        return mGraphScreen.getPaintColor();
    }

    /**
     * set draw {@link GraphObject} color
     *
     * @param color
     */
    public void setPaintGraphColor(int color) {
        mGraphScreen.setPaintColor(color);
    }

    //pascal
    public void closeGraph() {
        clearGraph();
        graphMode = false;
        postInvalidate();
    }

    //pascal
    public void clearGraph() {
        mGraphScreen.clear();
        postInvalidate();
    }

    //pascal
    public void setCursorGraphPosition(int x, int y) {
        mGraphScreen.setCursorPostion(x, y);
    }

    public ConsoleCursor getCursorGraph() {
        return mGraphScreen.getCursor();
    }

    public void setCursorGraphStyle(int style, int pattern, int width) {
        mGraphScreen.setPaintStyle(style, pattern, width);
    }

    public void setGraphBackground(int colorPascal) {
        mGraphScreen.setBackgroundColor(colorPascal);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        doShowSoftKeyboard();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
