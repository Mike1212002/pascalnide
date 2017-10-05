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

package com.duy.pascal.interperter.libraries.android.media.webcam;

import com.duy.pascal.interperter.libraries.android.media.JpegProvider;
import com.googlecode.sl4a.SimpleServer;
import com.googlecode.sl4a.event.Event;

import java.io.OutputStream;
import java.net.Socket;

public class MjpegServer extends SimpleServer {

    private final JpegProvider mProvider;

    public MjpegServer(JpegProvider provider) {
        mProvider = provider;
    }

    @Override
    protected void handleConnection(Socket socket) throws Exception {
        byte[] data = mProvider.getJpeg();
        if (data == null) {
            return;
        }
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((
                "HTTP/1.0 200 OK\r\n" +
                        "Server: SL4A\r\n" +
                        "Connection: close\r\n" +
                        "Max-Age: 0\r\n" +
                        "Expires: 0\r\n" +
                        "Cache-Control: no-cache, private\r\n" +
                        "Pragma: no-cache\r\n" +
                        "Content-Type: multipart/x-mixed-replace; boundary=--BoundaryString\r\n\r\n").getBytes());
        while (true) {
            data = mProvider.getJpeg();
            if (data == null) {
                return;
            }
            outputStream.write("--BoundaryString\r\n".getBytes());
            outputStream.write("Content-type: image/jpg\r\n".getBytes());
            outputStream.write(("Content-Length: " + data.length + "\r\n\r\n").getBytes());
            outputStream.write(data);
            outputStream.write("\r\n\r\n".getBytes());
            outputStream.flush();
        }
    }

    @Override
    public void onEventReceived(Event event) {

    }
}