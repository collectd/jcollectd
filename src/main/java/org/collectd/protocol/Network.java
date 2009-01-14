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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Constants from collectd/src/network.h and jcd.* property configuration.
 */
public class Network {
    public static final int DEFAULT_PORT         = 25826;
    public static final String DEFAULT_V4_ADDR   = "239.192.74.66";
    public static final String DEFAULT_V6_ADDR   = "ff18::efc0:4a42";

    public static final int TYPE_HOST            = 0x0000;
    public static final int TYPE_TIME            = 0x0001;
    public static final int TYPE_PLUGIN          = 0x0002;
    public static final int TYPE_PLUGIN_INSTANCE = 0x0003;
    public static final int TYPE_TYPE            = 0x0004;
    public static final int TYPE_TYPE_INSTANCE   = 0x0005;
    public static final int TYPE_VALUES          = 0x0006;
    public static final int TYPE_INTERVAL        = 0x0007;

    public static final int TYPE_MESSAGE         = 0x0100;
    public static final int TYPE_SEVERITY        = 0x0101;

    public static final int DS_TYPE_COUNTER = 0;
    public static final int DS_TYPE_GAUGE   = 1;

    static final int UINT8_LEN  = 1;
    static final int UINT16_LEN = UINT8_LEN * 2;
    static final int UINT32_LEN = UINT16_LEN * 2;
    static final int UINT64_LEN = UINT32_LEN * 2;
    static final int HEADER_LEN = UINT16_LEN * 2;
    static final int BUFFER_SIZE = 1024; // as per collectd/src/network.c

    private static final Properties _props = new Properties();
    private static final String KEY_PREFIX = "jcd.";

    static {
        loadProperties();
    }

    public static Properties getProperties() {
        return _props;
    }

    public static String getProperty(String name, String defval) {
        String key = KEY_PREFIX + name;
        return _props.getProperty(key, System.getProperty(key, defval));
    }

    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    private static void loadProperties() {
        String fname = KEY_PREFIX + "properties";
        String file = System.getProperty(fname, fname);

        if (new File(file).exists()) {
            Logger log = Logger.getLogger(Network.class.getName());
            FileInputStream is = null;
            try {
                is = new FileInputStream(file);
                _props.load(is);
                log.fine("Loaded " + file);
            } catch (IOException e) {
                log.fine("Unable to load " + file + ": " + e);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {}
                }
            }
        }
    }
}
