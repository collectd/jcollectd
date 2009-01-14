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

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

/**
 * Container for MBeanAttribute objects and
 * collectd value_list_t structure metadata mapping.  
 */
public class MBeanQuery {
    private String _plugin;
    private String _pluginInstance;
        
    private ObjectName _name;
    private String _alias;
    private Set<MBeanAttribute> _attributes =
        new HashSet<MBeanAttribute>();

    public MBeanQuery(ObjectName name) {
        _name = name;
    }

    public String getPlugin() {
        return _plugin;
    }

    public void setPlugin(String plugin) {
        _plugin = plugin;
    }

    public String getPluginInstance() {
        return _pluginInstance;
    }

    public void setPluginInstance(String pluginInstance) {
        _pluginInstance = pluginInstance;
    }

    public ObjectName getName() {
        return _name;
    }

    public void setName(ObjectName name) {
        _name = name;
    }

    public String getAlias() {
        return _alias;
    }

    public void setAlias(String alias) {
        _alias = alias;
    }

    public Set<MBeanAttribute> getAttributes() {
        return _attributes;
    }

    public void setAttributes(Set<MBeanAttribute> attributes) {
        _attributes = attributes;
    }

    public void addAttribute(MBeanAttribute attribute) {
        _attributes.add(attribute);
    }
}
