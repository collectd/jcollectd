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

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;
import java.util.logging.Logger;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Out-of-process MBeanSender, polling a remote JMX MBeanServer. 
 */
public class RemoteMBeanSender extends MBeanSender {
    private static final Logger _log =
        Logger.getLogger(RemoteMBeanSender.class.getName());

    public void setMBeanServerConnection(JMXServiceURL url)
        throws IOException {

        JMXConnector connector =
            JMXConnectorFactory.connect(url);
        setMBeanServerConnection(connector.getMBeanServerConnection());
    }

    public void setMBeanServerConnection(String url)
        throws IOException {

        if (url.indexOf('/') == -1) {
            url =
                "service:jmx:rmi:///jndi/rmi://" +
                url +
                "/jmxrmi";
        }

        _log.fine("URL=" + url);
        setMBeanServerConnection(new JMXServiceURL(url));
    }

    public void configure(Properties props) {
        String url = props.getProperty("jcd.mx.url");
        if (url != null) {
            try {
                setMBeanServerConnection(url);
            } catch (IOException e) {
                _log.warning(url + ": " + e);
            }
        }
        super.configure(props);
    }

    public static void premain(String args, Instrumentation instr) {
        MBeanSender sender = new RemoteMBeanSender();
        sender.premainConfigure(args);
    }
}
