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
import java.util.List;

import org.collectd.api.Notification;
import org.collectd.api.ValueList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ValueListTest extends TestCase {

    private static final String HOST = "localhost";
    private static final String PLUGIN = "collectd";
    private double values[] = {
       1.0, 0.2, 30.0
    };
    private long interval = 10;
    private static final long now = 1226466789000L;

    public ValueListTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ValueListTest.class);
    }

    private ValueList dummyValueList() {
        ValueList vl = new ValueList();
        vl.setHost(HOST);
        vl.setInterval(interval);
        vl.setTime(now);
        vl.setPlugin(PLUGIN);
        vl.setPluginInstance(ValueListTest.class.getName());
        for (int i=0; i<values.length; i++) {
            vl.addValue(new Double(values[i]));
        }
        return vl;
    }

    private void dummyAssert(ValueList vl) {
        assertEquals(vl.getHost(), HOST);
        assertEquals(vl.getInterval(), interval);
        assertEquals(vl.getTime(), now);
        assertEquals(vl.getPlugin(), PLUGIN);
        List<Number> vals = vl.getValues();
        for (int i=0; i<values.length; i++) {
            assertEquals(vals.get(i).doubleValue(), values[i]);
        }        
    }

    public void testCreate() {
        dummyAssert(dummyValueList());
    }

    private final class TestPacketDispatcher
        implements Dispatcher {

        public void dispatch(Notification notification) {
            //XXX
        }

        public void dispatch(ValueList vl) {
            dummyAssert(vl);
        }        
    }

    public void testWriter() throws IOException {
        ValueList vl = dummyValueList();
        PacketWriter pw = new PacketWriter();
        pw.write(vl);
        UdpReceiver receiver = new UdpReceiver();
        TestPacketDispatcher dispatcher = new TestPacketDispatcher();
        receiver.setDispatcher(dispatcher);
        receiver.parse(pw.getBytes());
    }
}
