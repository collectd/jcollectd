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

import java.io.File;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.collectd.protocol.TypesDB;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Convert jcollectd.xml filters to MBeanCollect objects. 
 */
public class MBeanConfig {
    private XPath _xpath = XPathFactory.newInstance().newXPath();

    private String getAttribute(Node node, String name) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) {
            return null;
        }
        Node item = attrs.getNamedItem(name);
        if (item == null) {
            return null;
        }
        return item.getNodeValue();
    }

    private NodeList eval(String name, Node node)
        throws XPathExpressionException {
        return (NodeList)_xpath.evaluate(name, node, XPathConstants.NODESET);
    }
 
    private NodeList eval(String name, InputSource is)
        throws XPathExpressionException {
        return (NodeList)_xpath.evaluate(name, is, XPathConstants.NODESET);
    }

    public MBeanCollector add(String source) throws Exception {
        String name = source + "-jcollectd.xml";
        if (new File(source).exists()) {
            return add(new InputSource(source));
        }
        else if (new File(name).exists()) {
            return add(new InputSource(name));
        }
        else {
            String[] rs = { name, "etc/" + name, "META-INF/" + name };
            for (int i=0; i<rs.length; i++) {
                InputStream is =
                    getClass().getClassLoader().getResourceAsStream(rs[i]);
                if (is != null) {
                    return add(is);
                }
            }
            return null;
        }
    }

    public MBeanCollector add(InputStream is) throws Exception {
        return add(new InputSource(is));
    }

    public MBeanCollector add(InputSource is) throws Exception {
        MBeanCollector collector = new MBeanCollector(); 
        final String path = "/jcollectd-config/mbeans";
        NodeList plugins = eval(path, is);

        int len = plugins.getLength();
        if (len == 0) {
            throw new IllegalArgumentException("Missing " + path);
        }

        for (int i=0; i<len; i++) {
            Node plugin = plugins.item(i);
            String pluginName = getAttribute(plugin, "name");
            NodeList mbeans = eval("mbean", plugin);

            for (int j=0; j<mbeans.getLength(); j++) {
                Node mbean = mbeans.item(j);
                String objectName = getAttribute(mbean, "name");
                String objectNameAlias = getAttribute(mbean, "alias");
                NodeList attrs = eval("attribute", mbean);
                MBeanQuery query = collector.addMBean(objectName);

                if (pluginName != null) {
                    query.setPlugin(pluginName);
                }
                if (objectNameAlias != null) {
                    query.setAlias(objectNameAlias);
                }

                for (int k=0; k<attrs.getLength(); k++) {
                    Node attr = attrs.item(k);
                    String attrName = getAttribute(attr, "name");
                    String type = getAttribute(attr, "type");
                    String units = getAttribute(attr, "units");
                    String alias = getAttribute(attr, "alias");
                    if (type == null) {
                        type = TypesDB.NAME_GAUGE;
                    }
                    MBeanAttribute mattr = new MBeanAttribute(attrName, type);
                    if (alias != null) {
                        mattr.setName(alias);
                    }
                    if (units != null) {
                        //XXX
                    }
                    query.addAttribute(mattr);
                }
            }
        }        

        return collector;
    }
}
