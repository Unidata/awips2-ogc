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

import java.io.InputStream;
import java.util.Map;

import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.DOMParser;
import org.geotools.xml.Parser;
import org.w3c.dom.Document;

/**
 * Styled Layer Descriptor version 1.0.0 document parser
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
public class SldParser100 extends AbstractSldParser {

    protected SLDConfiguration config = new SLDConfiguration();

    public SldParser100() {
        super("1.0.0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.wms.style.SldParser#parse(java.io.InputStream)
     */
    @Override
    public StyledLayerDescriptor parse(InputStream in) throws Exception {
        return (StyledLayerDescriptor) new Parser(config).parse(in);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.wms.sld.SldParser#parse(org.w3c.dom.Document)
     */
    @Override
    public StyledLayerDescriptor parse(Document doc) throws Exception {
        Object obj = new DOMParser(config, doc).parse();
        if (obj instanceof StyledLayerDescriptor) {
            return (StyledLayerDescriptor) obj;
        } else if (obj instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) obj;
            Object sldObj = m.get("StyledLayerDescriptor");
            if (sldObj == null) {
                return null;
            }
            if (sldObj instanceof StyledLayerDescriptor) {
                return (StyledLayerDescriptor) sldObj;
            }
        }
        // give up
        throw new Exception("Unable to find sld in document");
    }

}
