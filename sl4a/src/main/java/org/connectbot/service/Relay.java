/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.service;

import com.googlecode.android_scripting.Log;

import org.apache.harmony.niochar.charset.additional.IBM437;
import org.connectbot.transport.AbsTransport;
import org.connectbot.util.EastAsianWidth;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import de.mud.terminal.vt320;

/**
 * @author Kenny Root
 */
public class Relay implements Runnable {

    private static final int BUFFER_SIZE = 4096;

    private static boolean useJNI = true;

    static {
        useJNI = EastAsianWidth.useJNI;
    }

    private TerminalBridge bridge;
    private Charset currentCharset;
    private CharsetDecoder decoder;
    private boolean isLegacyEastAsian = false;
    private AbsTransport transport;
    private vt320 buffer;
    private ByteBuffer byteBuffer;
    private CharBuffer charBuffer;
    private byte[] byteArray;
    private char[] charArray;

    public Relay(TerminalBridge bridge, AbsTransport transport, vt320 buffer, String encoding) {
        setCharset(encoding);
        this.bridge = bridge;
        this.transport = transport;
        this.buffer = buffer;
    }

    public void setCharset(String encoding) {
        Log.d("changing charset to " + encoding);
        Charset charset;
        if (encoding.equals("CP437")) {
            charset = new IBM437("IBM437", new String[]{"IBM437", "CP437"});
        } else {
            charset = Charset.forName(encoding);
        }

        if (charset == currentCharset || charset == null) {
            return;
        }

        CharsetDecoder newCd = charset.newDecoder();
        newCd.onUnmappableCharacter(CodingErrorAction.REPLACE);
        newCd.onMalformedInput(CodingErrorAction.REPLACE);

        currentCharset = charset;
        synchronized (this) {
            decoder = newCd;
        }
    }

    public void run() {
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        charBuffer = CharBuffer.allocate(BUFFER_SIZE);

    /* for both JNI and non-JNI method */
        byte[] wideAttribute = new byte[BUFFER_SIZE];

    /* non-JNI fallback method */
        float[] widths = null;

        if (!useJNI) {
            widths = new float[BUFFER_SIZE];
        }

        byteArray = byteBuffer.array();
        charArray = charBuffer.array();

        CoderResult result;

        int bytesRead = 0;
        byteBuffer.limit(0);
        int bytesToRead;
        int offset;
        int charWidth;

        try {
            while (true) {
                charWidth = bridge.charWidth;
                bytesToRead = byteBuffer.capacity() - byteBuffer.limit();
                offset = byteBuffer.arrayOffset() + byteBuffer.limit();
                bytesRead = transport.read(byteArray, offset, bytesToRead);

                if (bytesRead > 0) {
                    byteBuffer.limit(byteBuffer.limit() + bytesRead);

                    synchronized (this) {
                        result = decoder.decode(byteBuffer, charBuffer, false);
                    }

                    if (result.isUnderflow() && byteBuffer.limit() == byteBuffer.capacity()) {
                        byteBuffer.compact();
                        byteBuffer.limit(byteBuffer.position());
                        byteBuffer.position(0);
                    }

                    offset = charBuffer.position();

                    if (!useJNI) {
                        bridge.getPaint().getTextWidths(charArray, 0, offset, widths);
                        for (int i = 0; i < offset; i++) {
                            wideAttribute[i] = (byte) (((int) widths[i] != charWidth) ? 1 : 0);
                        }
                    } else {
                        EastAsianWidth.measure(charArray, 0, charBuffer.position(), wideAttribute,
                                isLegacyEastAsian);
                    }
                    buffer.putString(charArray, wideAttribute, 0, charBuffer.position());
                    charBuffer.clear();
                    bridge.redraw();
                }
            }
        } catch (IOException e) {
            Log.e("Problem while handling incoming data in relay thread", e);
        }
    }
}
