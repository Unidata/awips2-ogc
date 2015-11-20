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
package com.raytheon.uf.edex.wms.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Utility to apply geotools styles to coverage and feature data
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
public class StyleUtility {

    public static BufferedImage applyStyle(GridCoverage2D coverage, Style style) {
        MapContent context = new MapContent();
        try {
            context.addLayer(new GridCoverageLayer(coverage, style));
            GridGeometry2D geom = coverage.getGridGeometry();
            Rectangle rec = geom.getGridRange2D();
            return mapToImage(context, rec);
        } finally {
            if (context != null) {
                context.dispose();
            }
        }
    }

    public static BufferedImage applyStyle(
            FeatureCollection<SimpleFeatureType, SimpleFeature> coll,
            Style style, Rectangle imageDims, ReferencedEnvelope mapBounds) {
        MapContent map = new MapContent();
        try {
            map.addLayer(new FeatureLayer(coll, style));
            return mapToImage(map, imageDims, mapBounds);
        } finally {
            if (map != null) {
                map.dispose();
            }
        }
    }

    public static BufferedImage mapToImage(MapContent map, Rectangle imageDims,
            ReferencedEnvelope mapBounds) {
        return mapToImage(map, imageDims, mapBounds, null);
    }

    public static BufferedImage mapToImage(MapContent map, Rectangle imageDims,
            Color bgcolor) {
        return mapToImage(map, imageDims, map.getViewport().getBounds(),
                bgcolor);
    }

    public static BufferedImage mapToImage(MapContent map, Rectangle imageDims,
            ReferencedEnvelope mapBounds, Color bgcolor) {
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);
        BufferedImage image = new BufferedImage(imageDims.width,
                imageDims.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = image.createGraphics();
        if (bgcolor != null) {
            gr.setColor(bgcolor);
            gr.fill(imageDims);
        }
        renderer.paint(gr, imageDims, mapBounds);
        gr.dispose();
        return image;
    }

    public static BufferedImage mapToImage(MapContent map, Rectangle imageDims) {
        return mapToImage(map, imageDims, map.getViewport().getBounds());
    }
}
