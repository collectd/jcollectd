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
import java.net.InetAddress;
import java.net.MulticastSocket;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MulticastReceiverTest extends ReceiverTest {

    public static Test suite() {
        return new TestSuite(MulticastReceiverTest.class);
    }

    @Override
    protected DatagramSocket createSocket() throws IOException {
        MulticastSocket socket = new MulticastSocket(Network.DEFAULT_PORT+100);
        String laddr = Network.DEFAULT_V4_ADDR;
        getReceiver().setListenAddress(laddr);
        socket.joinGroup(InetAddress.getByName(laddr));
        return socket;
    }
}
