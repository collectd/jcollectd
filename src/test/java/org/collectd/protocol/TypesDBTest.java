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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for TypesDB.
 */
public class TypesDBTest 
    extends TestCase
{
    public TypesDBTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TypesDBTest.class);
    }

    public void testLoad() {
        TypesDB tl = new TypesDB();
        try {
            tl.load();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertTrue(tl.getTypes().size() > 0);
        assertTrue(TypesDB.getInstance().getTypes().size() > 0);
    }
}
