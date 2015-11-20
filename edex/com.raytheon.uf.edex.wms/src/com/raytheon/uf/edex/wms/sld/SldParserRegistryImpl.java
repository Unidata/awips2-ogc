/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.wms.sld;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that controls access to Styled Layer Descriptor document parsers
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 28, 2012            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class SldParserRegistryImpl implements SldParserRegistry {

    protected Map<String, SldParser> parsers = new ConcurrentHashMap<String, SldParser>();

    @Override
    public SldParserRegistry register(SldParser parser) {
        // allow for overriding of previously registered parsers
        parsers.put(parser.getVersion(), parser);
        return this;
    }

    @Override
    public SldParserRegistry unregister(SldParser parser) {
        parsers.remove(parser.getVersion());
        return this;
    }

    @Override
    public SldParser getParser(String version) {
        if (version == null) {
            return null;
        }
        return parsers.get(version);
    }

    @Override
    public SldParserRegistry register(SldParser[] parsers) {
        for (SldParser p : parsers) {
            register(p);
        }
        return this;
    }

}
