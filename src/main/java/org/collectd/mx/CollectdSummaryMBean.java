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

import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Aggregate metric values and return the average as the attribute values. 
 */
public class CollectdSummaryMBean extends CollectdMBean {

    private ObjectName _query;
    private CollectdMBeanRegistry _registry;

    public CollectdSummaryMBean(ObjectName name,
                                Map<String, Number> metrics) {
        super(metrics);
        _query = name;
    }

    void setMBeanRegistry(CollectdMBeanRegistry registry) {
        _registry = registry;
    }

    private Object getAverage(String key) {
        double sum = 0;
        double avg;
        int num = 0;
        Set<ObjectName> names = _registry.bs.queryNames(_query, null);

        for (ObjectName name : names) {
            Number val = _registry.getMBeanAttribute(name, key);
            if (val == null) {
                continue;
            }
            num++;
            sum += val.doubleValue();
        }

        if (num == 0) {
            avg = 0;
        }
        else {
            avg = sum / num;
        }

        return new Double(avg);
    }

    public Object getAttribute(String key)
        throws AttributeNotFoundException,
               MBeanException,
               ReflectionException {

        try {
            return getAverage(key);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getAttributeType(String name) {
        return Double.class.getName();
    }

    protected String getAttributeDescription(String name) {
        return super.getAttributeDescription(name) + " (summary)";
    }
}
