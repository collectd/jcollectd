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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

import org.collectd.api.PluginData;
import org.collectd.api.ValueList;
import org.collectd.api.Notification;

/**
 * collectd UDP protocol receiver.
 * See collectd/src/network.c:parse_packet
 */
public class UdpReceiver {

    private static final Logger _log =
        Logger.getLogger(UdpReceiver.class.getName());
    private Dispatcher _dispatcher;
    private DatagramSocket _socket;
    private int _port = Network.DEFAULT_PORT;
    private String _bindAddress;
    private String _ifAddress;
    private boolean _isShutdown = false;

    protected UdpReceiver() {
        String addr = Network.getProperty("laddr", Network.DEFAULT_V4_ADDR);
        if (addr != null) {
            int ix = addr.indexOf(':'); //XXX ipv6
            if (ix == -1) {
                _bindAddress = addr;
            }
            else {
                _bindAddress = addr.substring(0, ix);
                _port = Integer.parseInt(addr.substring(ix+1));
            }
        }
        addr = Network.getProperty("ifaddr");
        if (addr != null) {
            try {
                //-Djcd.ifaddr=tun0
                _ifAddress =
                    NetworkInterface.getByName(addr).getInetAddresses().
                        nextElement().getHostAddress();
            } catch (Exception e) {
                //-Djcd.ifaddr=10.2.0.43
                _ifAddress = addr;
            }
            _log.fine("Using interface address=" + _ifAddress);
        }
    }

    public UdpReceiver(Dispatcher dispatcher) {
        this();
        setDispatcher(dispatcher);
    }

    public void setDispatcher(Dispatcher dispatcher) {
        _dispatcher = dispatcher;       
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        _port = port;
    }

    public String getListenAddress() {
        return _bindAddress;
    }

    public void setListenAddress(String address) {
        _bindAddress = address;
    }

    public String getInterfaceAddress() {
        return _ifAddress;
    }

    public void setInterfaceAddress(String address) {
        _ifAddress = address;
    }

    public DatagramSocket getSocket() throws IOException {
        if (_socket == null) {
            if (_bindAddress == null) {
                _socket = new DatagramSocket(_port);
            }
            else {
                InetAddress addr = InetAddress.getByName(_bindAddress);
                if (addr.isMulticastAddress()) {
                    MulticastSocket mcast = new MulticastSocket(_port);
                    if (_ifAddress != null) {
                        mcast.setInterface(InetAddress.getByName(_ifAddress));
                    }
                    mcast.joinGroup(addr);
                    _socket = mcast;
                }
                else {
                    _socket = new DatagramSocket(_port, addr);
                }
            }
        }
        return _socket;
    }

    public void setSocket(DatagramSocket socket) {
        _socket = socket;
    }

    private String readString(DataInputStream is, int len)
        throws IOException {
        byte[] buf = new byte[len];
        is.read(buf, 0, len);
        return new String(buf, 0, len-1); //-1 -> skip \0
    }

    private void readValues(DataInputStream is, ValueList vl)
        throws IOException {
        byte[] dbuff = new byte[8];
        int nvalues = is.readUnsignedShort();
        int[] types = new int[nvalues];
        for (int i=0; i<nvalues; i++) {
            types[i] = is.readByte();
        }
        for (int i=0; i<nvalues; i++) {
            Number val;
            if (types[i] == Network.DS_TYPE_COUNTER) {
                val = new Long(is.readLong());
            }
            else {
                //collectd uses x86 host order for doubles
                is.read(dbuff);
                ByteBuffer bb = ByteBuffer.wrap(dbuff);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                val = new Double(bb.getDouble());
            }
            vl.addValue (val);
        }
        if (_dispatcher != null) {
            _dispatcher.dispatch(vl);
        }
        vl.clearValues ();
    }

    //a union of sorts
    private static class PacketObject {
        ValueList vl;
        Notification notif;
        PluginData pd = new PluginData();

        ValueList getValueList() {
            if (vl == null) {
                vl = new ValueList(pd);
                pd = vl;
            }
            return vl;
        }

        Notification getNotification() {
            if (notif == null) {
                notif = new Notification(pd);
                pd = notif;
            }
            return notif;
        }
    }

    public void parse(byte[] packet) throws IOException {
        int total = packet.length;
        ByteArrayInputStream buffer =
            new ByteArrayInputStream(packet);
        DataInputStream is =
            new DataInputStream(buffer);
        PacketObject obj = new PacketObject();

        while ((0 < total) && (total > Network.HEADER_LEN)) {
            int type = is.readUnsignedShort();
            int len = is.readUnsignedShort();

            if (len < Network.HEADER_LEN) {
                break; //packet was filled to the brim
            }

            total -= len;
            len -= Network.HEADER_LEN;

            if (type == Network.TYPE_VALUES) {
                readValues(is, obj.getValueList());
            }
            else if (type == Network.TYPE_TIME) {
                obj.pd.setTime (is.readLong() * 1000);
            }
            else if (type == Network.TYPE_INTERVAL) {
                obj.getValueList ().setInterval (is.readLong ());
            }
            else if (type == Network.TYPE_HOST) {
                obj.pd.setHost (readString (is, len));
            }
            else if (type == Network.TYPE_PLUGIN) {
                obj.pd.setPlugin (readString (is, len));
            }
            else if (type == Network.TYPE_PLUGIN_INSTANCE) {
                obj.pd.setPluginInstance (readString (is, len));
            }
            else if (type == Network.TYPE_TYPE) {
                obj.pd.setType (readString (is, len));
            }
            else if (type == Network.TYPE_TYPE_INSTANCE) {
                obj.pd.setTypeInstance (readString (is, len));
            }
            else if (type == Network.TYPE_MESSAGE) {
                Notification notif = obj.getNotification();
                notif.setMessage (readString(is, len));
                if (_dispatcher != null) {
                    _dispatcher.dispatch(notif);
                }
            }
            else if (type == Network.TYPE_SEVERITY) {
                obj.getNotification ().setSeverity ((int) is.readLong ());
            }
            else {
                break;
            }
        }
    }

    public void listen() throws Exception {
        listen(getSocket());
    }

    public void listen(DatagramSocket socket) throws IOException { 
        while (true) {
            byte[] buf = new byte[Network.BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (SocketException e) {
                if (_isShutdown) {
                    break;
                }
                else {
                    throw e;
                }
            }
            parse(packet.getData());
        }        
    }

    public void shutdown() {
        if (_socket != null) {
            _isShutdown = true;
            _socket.close();
            _socket = null;
        }
    }

    public static void main(String[] args) throws Exception {
        new UdpReceiver(new StdoutDispatcher()).listen();
    }
}
