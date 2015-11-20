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
package com.raytheon.uf.edex.wms.reg;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridGeometry2D;
import org.opengis.feature.simple.SimpleFeature;

import com.raytheon.uf.edex.ogc.common.OgcLayer;
import com.raytheon.uf.edex.wms.WmsException;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Interface for providing the OGC Web Map Service access to plugin data
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
public interface WmsSource {

    /**
     * @return a Collection of OgcLayers. Every layer in the collection must use
     *         the same key in the layer name.
     */
    public Collection<OgcLayer> listLayers();

    /**
     * @param layerName
     * @return null if layer is not found
     */
    public OgcLayer getLayer(String layerName) throws WmsException;

    public WmsImage getImage(String layer, String style, boolean defaultStyle,
            GridGeometry2D targetGeom, String time, String elevation,
            Map<String, String> dimensions, double scale) throws WmsException;

    public boolean hasUpdated();

    public List<SimpleFeature> getFeatureInfo(String layer,
            GridGeometry2D targetGeom, String time,
            String elevation, Map<String, String> dimensions, Coordinate c,
            double scale) throws WmsException;

    public BufferedImage getLegend(String layer, String style, String time,
            String elevation, Map<String, String> dimensions, Integer height,
            Integer width) throws WmsException;

    /**
     * @return a unique key that identifies this source
     */
    public String getKey();

    public boolean isWmtsCapable();

}
