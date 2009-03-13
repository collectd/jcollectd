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

import java.util.List;

import org.collectd.api.DataSource;
import org.collectd.protocol.Network;
import org.collectd.protocol.TypesDB;

/**
 * MBean attribute to collectd metric metadata mapper.
 */
public class MBeanAttribute {

    private static final TypesDB _types = TypesDB.getInstance();
    private String _name;
    private String _attributeName;
    private String _compositeKey;
    private String _typeName;
    private int _dataType;

    public MBeanAttribute(String attributeName) {
        this(attributeName, Network.DS_TYPE_GAUGE);
    }

    public MBeanAttribute(String attributeName, int dataType) {
        this(attributeName, dataType, null);
    }

    private static final int getDataType(String typeName) {
        List<DataSource> ds = _types.getType(typeName); 
        if ((ds == null) || (ds.size() == 0)) {
            return Network.DS_TYPE_GAUGE;
        }
        else {
            return ds.get(0).getType();
        }
    }

    public MBeanAttribute(String attributeName, String typeName) {
        this(attributeName, getDataType(typeName), typeName);
    }

    public MBeanAttribute(String attributeName, int dataType, String typeName) {
        _name = attributeName;
        _dataType = dataType;
        _typeName = typeName;
        if (_typeName == null) {
            if (dataType == Network.DS_TYPE_COUNTER) {
                _typeName = TypesDB.NAME_COUNTER;
            }
            else {
                _typeName = TypesDB.NAME_GAUGE;
            }
        }
        int ix = attributeName.indexOf('.');
        if (ix != -1) {
            _attributeName = attributeName.substring(0, ix);
            _compositeKey  = attributeName.substring(ix+1);
        }
        else {
            _attributeName = attributeName;
        }
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getAttributeName() {
        return _attributeName;
    }

    public void setAttributeName(String attributeName) {
        _attributeName = attributeName;
    }

    public String getCompositeKey() {
        return _compositeKey;
    }

    public void setCompositeKey(String compositeKey) {
        _compositeKey = compositeKey;
    }

    public String getTypeName() {
        return _typeName;
    }

    public void setTypeName(String typeName) {
        _typeName = typeName;
    }

    public int getDataType() {
        return _dataType;
    }

    public void setDataType(int dataType) {
        _dataType = dataType;
    }
}
