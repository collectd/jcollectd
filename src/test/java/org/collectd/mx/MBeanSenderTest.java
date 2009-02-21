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

package org.collectd.mx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.collectd.api.Notification;
import org.collectd.api.ValueList;
import org.collectd.protocol.Dispatcher;
import org.collectd.protocol.ReceiverTest;
import org.collectd.protocol.UdpReceiver;

public class MBeanSenderTest
    extends TestCase
    implements Dispatcher {

    private static Logger _log =
        Logger.getLogger(MBeanSenderTest.class.getName());
    private MBeanServer _bs =
        ManagementFactory.getPlatformMBeanServer();
    private static final String PLUGIN = "MBeanSenderTest";
    private MBeanSender _sender;
    private ReceiverTest _receiverTest;
    private int _numValues = 0;
    private int _numMyValues = 0;
    private int _numNotif = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _receiverTest = new ReceiverTest();
        _receiverTest.setUp();
        UdpReceiver receiver = _receiverTest.getReceiver();
        receiver.setDispatcher(this);
        int port = receiver.getSocket().getLocalPort();
        _sender = new MBeanSender();
        _sender.addDestination("udp://localhost:" + port);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        _receiverTest.tearDown();
        _log.info(_numValues + " ValueLists dispatched");
        _log.info(_numNotif + " Notifications dispatched");
    }

    private static void pause() throws Exception {
        Thread.sleep(1000);
    }

    public void testJavaLangMBeans() throws Exception {
        ObjectName name = new ObjectName("java.lang:*");
        MBeanCollector collector = new MBeanCollector();
        Set<ObjectName> names = _bs.queryNames(name, null);
        for (ObjectName on : names) {
            collector.addMBean(on.getCanonicalName());
        }
        _sender.schedule(collector);
        _sender.flush();
        pause();
    }

    public void testJavaLangTemplate() throws Exception {
        MBeanConfig config = new MBeanConfig();
        MBeanCollector collector = config.add("javalang");
        assertNotNull(collector);
        assertTrue(collector.getQueries().size() > 1);
    }

    public void testDynamicMBean() throws Exception {
        Map<String,Number> attrs = new HashMap<String,Number>();
        int n = _numMyValues;
        attrs.put("Foo", new Long(1));
        attrs.put("Bar", new Long(2));
        CollectdMBean mbean = new CollectdMBean(attrs);
        ObjectName name = new ObjectName(PLUGIN + ":type=dynamic");
        ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, name);
        _sender.scheduleMBean(name.getCanonicalName());
        _sender.flush();
        pause();
        _log.info(name + " was=" + n + ", now=" + _numMyValues);
        assertTrue(_numMyValues > n);
    }

    public void dispatch(Notification notification) {
        _numNotif++;
    }

    public void dispatch(ValueList vl) {
        _numValues++;
        if (vl.getPlugin().equals(PLUGIN)) {
            _numMyValues++;
        }
    }
}
