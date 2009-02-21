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
import java.net.InetAddress;

import org.collectd.api.PluginData;
import org.collectd.api.ValueList;
import org.collectd.api.Notification;

/**
 * Protocol independent Sender interface. 
 */
public abstract class Sender implements Dispatcher {

    private String _host =
        Network.getProperty("host", Network.getProperty("hostname"));

    public Sender() {
    }

    protected abstract void write(PluginData data) throws IOException;

    public abstract void flush() throws IOException;

    public abstract void addServer(String server);

    public String getHost() {
        if (_host == null) {
            try {
                _host =
                    InetAddress.getLocalHost().getHostName();
            } catch (IOException e) {
                _host = "unknown";
            }       
        }
        return _host;
    }

    public void setHost(String host) {
        _host = host;
    }

    protected void setDefaults(PluginData data) {
        if (data.getHost() == null) {
            data.setHost(getHost());
        }
        if (data.getTime() <= 0) {
            data.setTime(System.currentTimeMillis());
        }
    }

    public void dispatch(ValueList values) {
        try {
            setDefaults(values);
            write(values);
        } catch (IOException e) {
            //XXX
            e.printStackTrace();
        }
    }

    public void dispatch(Notification notification) {
        try {
            setDefaults(notification);
            write(notification);
        } catch (IOException e) {
            //XXX
            e.printStackTrace();
        }
    }
}
