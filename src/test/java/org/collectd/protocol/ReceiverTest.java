/*
 * jcollectd
 * Copyright (C) 2009 Hyperic, Inc.
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; only version 2 of the License is applicable.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package org.collectd.protocol;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ReceiverTest extends TestCase {

    private UdpReceiver _receiver;
    private Thread _receiverThread;
    private static Exception _error;

    public static Test suite() {
        return new TestSuite(ReceiverTest.class);
    }

    protected Logger getLog() {
        return Logger.getLogger(getClass().getName());
    }

    public UdpReceiver getReceiver() {
        return _receiver;
    }

    private class ReceiverThread extends Thread {
        private ReceiverThread() {
            super("CollectdReceiverThread");
        }

        public void run() {
            try {
                _receiver.listen();
            } catch (Exception e) {
                _error = e;
            }
        }        
    }

    public void testBound() throws Exception {
        DatagramSocket socket = getReceiver().getSocket();
        assertTrue(socket.isBound());
        getLog().info("Bound to LocalPort=" + socket.getLocalPort() +
                      ", LocalAddress=" +
                      socket.getLocalAddress().getHostAddress());
    }

    protected DatagramSocket createSocket() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        _receiver.setListenAddress("127.0.0.1");
        return socket;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _receiver = new UdpReceiver();
        DatagramSocket socket = createSocket();
        _receiver.setSocket(socket);
        _receiverThread = new ReceiverThread();
        _receiverThread.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        _receiver.shutdown();
        _receiverThread.interrupt();
        if (_error != null) {
            _error.printStackTrace();
            throw _error; //propagate listener thread exception
        }
    }
}
