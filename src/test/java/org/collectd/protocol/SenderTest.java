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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.collectd.api.Notification;
import org.collectd.api.ValueList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SenderTest
    extends TestCase
    implements Dispatcher {

    private static long INTERVAL = 10;
    private static String PLUGIN = "junit";
    private static String PLUGIN_INSTANCE = "SenderTest";
    private static final String TYPE = "test";

    private double dvals[] = { 1.0, 66.77, Double.MAX_VALUE };
    private long lvals[] = { 1, 66, Long.MAX_VALUE, 4 };

    private List<ValueList> _values = new ArrayList<ValueList>();
    protected Sender _sender;
    private ReceiverTest _receiverTest;

    public static Test suite() {
        return new TestSuite(SenderTest.class);
    }

    protected Logger getLog() {
        return Logger.getLogger(getClass().getName());
    }

    protected ReceiverTest createReceiverTest() {
        ReceiverTest rtest = new ReceiverTest();
        return rtest;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _receiverTest = createReceiverTest();
        _receiverTest.setUp();
        UdpReceiver receiver = _receiverTest.getReceiver();
        receiver.setDispatcher(this);
        int port = receiver.getSocket().getLocalPort();
        _sender = new UdpSender();
        String dest = receiver.getListenAddress() + ":" + port;
        getLog().info("Add destination: " + dest);
        _sender.addServer(dest);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        _receiverTest.tearDown();
    }

    private ValueList newValueList() {
        ValueList vl = new ValueList();
        vl.setPlugin(PLUGIN);
        vl.setPluginInstance(PLUGIN_INSTANCE);
        vl.setInterval(INTERVAL);
        vl.setType(TYPE);
        return vl;
    }

    private void assertValueList(ValueList vl,
                                 String host, long time)
        throws Exception {

        assertTrue(vl.getHost().equals(host));
        assertTrue(vl.getTime()/1000 == time);
        assertTrue(vl.getInterval() == INTERVAL);
        assertTrue(vl.getPlugin().equals(PLUGIN));
        assertTrue(vl.getPluginInstance().equals(PLUGIN_INSTANCE));
        assertTrue(vl.getType().equals(TYPE));
    }

    private void flush() throws Exception {
        _sender.flush();
        Thread.sleep(500);
    }

    public void testGauge() throws Exception {
        ValueList vl = newValueList();
        for (double val : dvals) {
            vl.addValue(new Double(val));
        }
        _sender.dispatch(vl);
        String host = vl.getHost();
        long time = vl.getTime() / 1000;
        flush();
        assertTrue(_values.size() == 1);
        vl = _values.get(0);
        assertValueList(vl, host, time);
        assertTrue(vl.getValues().size() == dvals.length);
        int i=0;
        for (Number num : vl.getValues()) {
            assertTrue(num.getClass() == Double.class);
            assertTrue(num.doubleValue() == dvals[i++]);
        }
        _values.clear();
    }

    public void testCounter() throws Exception {
        ValueList vl = newValueList();
        for (long val : lvals) {
            vl.addValue(new Long(val));
        }
        _sender.dispatch(vl);
        String host = vl.getHost();
        long time = vl.getTime() / 1000;
        flush();
        assertTrue(_values.size() == 1);
        vl = _values.get(0);
        assertValueList(vl, host, time);
        assertTrue(vl.getValues().size() == lvals.length);
        int i=0;
        for (Number num : vl.getValues()) {
            assertTrue(num.getClass() == Long.class);
            assertTrue(num.longValue() == lvals[i++]);
        }
        _values.clear();
    }
    
    public void dispatch(Notification notification) {
        
    }

    public void dispatch(ValueList vl) {
        _values.add(new ValueList(vl));
    }
}
