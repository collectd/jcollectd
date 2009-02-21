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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.collectd.api.PluginData;

/**
 * collectd UDP protocol sender.
 * See collectd/src/network.c:network_write
 */
public class UdpSender extends Sender {
    private List<InetSocketAddress> _servers;
    private DatagramSocket _socket;
    private MulticastSocket _mcast;
    private PacketWriter _writer;
    
    public UdpSender() {
        _servers = new ArrayList<InetSocketAddress>();
        _writer = new PacketWriter();        
    }

    public void addServer(String server) {
        String ip;
        int ix = server.indexOf(':');
        int port;
        if (ix == -1) {
            ip = server;
            port = Network.DEFAULT_PORT;
        }
        else {
            ip = server.substring(0, ix);
            port = Integer.parseInt(server.substring(ix+1));
        }
        addServer(new InetSocketAddress(ip, port));
    }

    public void addServer(InetSocketAddress server) {
        _servers.add(server);
    }

    private DatagramSocket getSocket() throws SocketException {
        if (_socket == null) {
            _socket = new DatagramSocket();
        }
        return _socket;
    }

    private MulticastSocket getMulticastSocket() throws IOException {
        if (_mcast == null) {
            _mcast = new MulticastSocket();
            _mcast.setTimeToLive(1);
        }
        return _mcast;
    }

    protected void write(PluginData data) throws IOException {
        setDefaults(data);
        int len = _writer.getSize();
        _writer.write(data);
        if (_writer.getSize() >= Network.BUFFER_SIZE) {
            send(_writer.getBytes(), len);
            _writer.reset();
            _writer.write(data);//redo XXX better way?
        }
    }

    private void send(byte[] buffer, int len) throws IOException {
        for (InetSocketAddress address : _servers) {
            DatagramPacket packet = 
                new DatagramPacket(buffer, len, address);
            if (address.getAddress().isMulticastAddress()) {
                getMulticastSocket().send(packet);
            }
            else {
                getSocket().send(packet);
            }
        }
    }

    public void flush() throws IOException {
        if (_writer.getSize() == 0) {
            return;
        }
        byte[] buffer = _writer.getBytes();
        send(buffer, buffer.length);
        _writer.reset();
    }
}
