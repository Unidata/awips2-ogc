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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.datastorage.records.IntegerDataRecord;
import com.raytheon.uf.edex.ogc.common.reprojection.ReferencedDataRecord;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.reg.WmsImage;

/**
 * Utility to handle styling request for pre-rendered (stored image) data
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------
 * Nov 28, 2012           bclement  Initial creation
 * Aug 30, 2016  5867     randerso  Updated for GeoTools 15.1
 *
 * </pre>
 *
 * @author bclement
 * @version 1.0
 */
public class PreRenderedStyleProvider extends SingleCoverageStyleProvider {

    public static final Style preRendered;

    static {
        StyleBuilder sb = new StyleBuilder();
        RasterSymbolizer symbolizer = sb.createRasterSymbolizer();
        symbolizer.setOpacity(new FilterFactoryImpl().literal(1.0));
        preRendered = sb.createStyle(symbolizer);
    }

    /**
     * @param style
     */
    public PreRenderedStyleProvider() {
        super(preRendered);
    }

    @Override
    public WmsImage styleData(IWmsDataRetriever retriever, WmsStyleChoice style,
            PluginDataObject record, GridGeometry2D geom) throws WmsException {
        ReferencedEnvelope env = new ReferencedEnvelope(geom.getEnvelope2D(),
                geom.getCoordinateReferenceSystem());
        ReferencedDataRecord ref = retriever.getDataRecord(record, env);
        if (ref == null) {
            return new WmsImage((GridCoverage2D) null);
        }
        IntegerDataRecord intrec = (IntegerDataRecord) ref.getRecord();
        long[] dims = intrec.getSizes();
        int w = (int) dims[0];
        int h = (int) dims[1];
        BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, w, h, intrec.getIntData(), 0, w);
        GridCoverageFactory fact = new GridCoverageFactory();
        return new WmsImage(fact.create("", img, ref.getEnvelope()),
                this.style);
    }

    @Override
    public BufferedImage getLegend(String layer, PluginDataObject pdo,
            String style, Integer width, Integer height) throws WmsException {
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        String label = "Pre-Rendered Imagery";
        Rectangle2D stringBounds = font.getStringBounds(label,
                new FontRenderContext(null, false, false));
        int strWidth = (int) stringBounds.getWidth();
        // extra 4 for bottom pad
        int strHeight = (int) (stringBounds.getHeight() + 4);
        BufferedImage rval = new BufferedImage(strWidth, strHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rval.createGraphics();
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(label, 0, (int) stringBounds.getHeight());
        g.dispose();
        return AbstractSldStyleProvider.resizeIfNeeded(rval, width, height);
    }

}
