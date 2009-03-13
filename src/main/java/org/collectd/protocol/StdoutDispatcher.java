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

import org.collectd.api.DataSource;
import org.collectd.api.ValueList;
import org.collectd.api.Notification;

/**
 * Dispatch collectd data to stdout.
 * java -classpath collectd.jar org.collectd.protocol.UdpReceiver 
 */
public class StdoutDispatcher implements Dispatcher {

    private boolean namesOnly =
        "true".equals(Network.getProperty("namesOnly"));

    public void dispatch(ValueList vl) {
        if (namesOnly) {
            System.out.print("plugin=" + vl.getPlugin());
            System.out.print(",pluginInstance=" + vl.getPluginInstance());
            System.out.print(",type=" + vl.getType());
            System.out.print(",typeInstance=" + vl.getTypeInstance());
            List<DataSource> ds = vl.getDataSource();
            if (ds == null) {
                ds = TypesDB.getInstance().getType(vl.getType());
            }
            if (ds != null) {
                List<String> names = new ArrayList<String>();
                for (int i=0; i<ds.size(); i++) {
                    names.add(ds.get(i).getName());
                }
                System.out.print("-->" + names);
            }
            System.out.println();
        }
        else {
            System.out.println(vl);
        }
    }

    public void dispatch(Notification notification) {
        System.out.println(notification);
    }
}
