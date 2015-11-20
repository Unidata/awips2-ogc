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
package com.raytheon.uf.edex.wms.styling;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.styling.Style;

import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.reg.WmsImage;

/**
 * Utility to style feature (point and shape) data that only has one valid style
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
public class SingleFeatureStyleProvider extends SingleStyleProvider implements
        IFeatureStyleProvider {

    /**
     * @param style
     */
    public SingleFeatureStyleProvider(Style style) {
        super(style);
    }

    /**
     * @param styleInfo
     * @param style
     */
    public SingleFeatureStyleProvider(OgcStyle styleInfo, Style style) {
        super(styleInfo, style);
    }

    @Override
    public WmsImage styleData(MemoryFeatureCollection coll, String layer,
            String style, boolean defaultStyle) throws WmsException {
        return new WmsImage(coll, this.style);
    }

    @Override
    public BufferedImage getLegend(String layer, String style,
            Map<String, String> dimensions, Map<String, String> levelUnits,
            Integer width, Integer height) throws WmsException {
        return AbstractSldStyleProvider.getLegend(this.style, width, height);
    }

}
