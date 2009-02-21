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

/**
 * Java representation of collectd/src/plugin.h:data_source_t structure. 
 */
public class DataSource {
    public static final int TYPE_COUNTER = 0;
    public static final int TYPE_GAUGE   = 1;

    static final String COUNTER = "COUNTER";
    static final String GAUGE = "GAUGE";

    static final String NAN = "U";
    private static final String[] TYPES = { COUNTER, GAUGE };

    String _name;
    int _type;
    double _min;
    double _max;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public int getType() {
        return _type;
    }

    public void setType(int type) {
        _type = type;
    }

    public double getMin() {
        return _min;
    }

    public void setMin(double min) {
        _min = min;
    }

    public double getMax() {
        return _max;
    }

    public void setMax(double max) {
        _max = max;
    }

    static double toDouble(String val) {
        if (val.equals(NAN)) {
            return Double.NaN;
        }
        else {
            return Double.parseDouble(val);
        }
    }

    private String asString(double val) {
        if (Double.isNaN(val)) {
            return NAN;
        }
        else {
            return String.valueOf(val);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        final char DLM = ':';
        sb.append(_name).append(DLM);
        sb.append(TYPES[_type]).append(DLM);
        sb.append(asString(_min)).append(DLM);
        sb.append(asString(_max));
        return sb.toString();
    }
}
