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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.reg.WmsImage;

/**
 * Utility to style coverage (gridded) data that only has one valid style
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
public class SingleCoverageStyleProvider extends SingleStyleProvider implements
        ICoverageStyleProvider<PluginDataObject> {

    /**
     * @param style
     */
    public SingleCoverageStyleProvider(Style style) {
        super(style);
    }

    /**
     * @param styleInfo
     * @param style
     */
    public SingleCoverageStyleProvider(OgcStyle styleInfo, Style style) {
        super(styleInfo, style);
    }

    @Override
    public WmsImage styleData(IWmsDataRetriever retriever,
            WmsStyleChoice style, PluginDataObject record, GridGeometry2D geom)
            throws WmsException {
        ReferencedEnvelope env = new ReferencedEnvelope(geom.getEnvelope2D(),
                geom.getCoordinateReferenceSystem());
        GridCoverage2D cov = retriever.getGridCoverage(record, env);
        return new WmsImage(cov, this.style);
    }

    @Override
    public WmsStyleChoice getStyle(String layer, PluginDataObject pdo,
            String style) throws WmsException {
        return new WmsStyleChoice(this.style);
    }

    @Override
    public BufferedImage getLegend(String layer, PluginDataObject pdo,
            String style, Integer width, Integer height) throws WmsException {
        return SldStyleProvider.getLegend(this.style, width, height);
    }

    @Override
    public double convertToDisplay(String layer, PluginDataObject record,
            double value) throws WmsException {
        return value;
    }

}
