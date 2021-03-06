/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.connect.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier.InvalidCLID;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;

public class TestIDGens {

    private static final String TOTO_TITI = "toto--titi";

    private static final Log log = LogFactory.getLog(TestIDGens.class);

    @Test
    public void testCTIDGen() {
        TechnicalInstanceIdentifier ctid = new TechnicalInstanceIdentifier();
        String ctId1 = ctid.getCTID();
        TechnicalInstanceIdentifier ctid2 = new TechnicalInstanceIdentifier();
        String ctId2 = ctid2.getCTID();
        log.info(ctId1);
        assertEquals(ctId1, ctId2);
    }

    protected void dotestCLID(LogicalInstanceIdentifier CLID) throws IOException, InvalidCLID {
        CLID.save();
        LogicalInstanceIdentifier CLID2 = LogicalInstanceIdentifier.load();
        assertNotNull(CLID2);
        assertEquals(CLID.getCLID1(), CLID2.getCLID1());
        assertEquals(CLID.getCLID2(), CLID2.getCLID2());
        assertEquals(CLID.getInstanceDescription(), CLID2.getInstanceDescription());
    }

    @Test
    public void testCLIDPlainText() throws InvalidCLID, IOException {
        LogicalInstanceIdentifier.USE_BASE64_SAVE = false;
        LogicalInstanceIdentifier CLID = new LogicalInstanceIdentifier(TOTO_TITI, "myInstance");
        dotestCLID(CLID);
    }

    @Test
    public void testCLIDEncoded() throws InvalidCLID, IOException {
        LogicalInstanceIdentifier.USE_BASE64_SAVE = true;
        LogicalInstanceIdentifier CLID = new LogicalInstanceIdentifier(TOTO_TITI, "myInstance");
        dotestCLID(CLID);
    }

    @Test
    public void testCanReloadEmptyDescriptionCLID() throws InvalidCLID, IOException {
        LogicalInstanceIdentifier CLID = new LogicalInstanceIdentifier(TOTO_TITI);
        dotestCLID(CLID);
    }

    @Test(expected = InvalidCLID.class)
    public void testInvalidCLID() throws IOException, InvalidCLID {
        Path tempCLID = Files.createTempFile("instance", ".clid");
        Files.write(tempCLID, "invalid CLID with only one line".getBytes());
        try {
            LogicalInstanceIdentifier.load(tempCLID.toString());
        } finally {
            Files.delete(tempCLID);
        }
    }

}
