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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginProperties;
import com.raytheon.uf.edex.ogc.common.IStyleLookupCallback;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.StyleLookup;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer;
import com.raytheon.uf.edex.ogc.common.db.SimpleDimension;
import com.raytheon.uf.edex.ogc.common.db.SimpleLayer;
import com.raytheon.uf.edex.ogc.common.feature.FeatureFactory;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;
import com.raytheon.uf.edex.wms.styling.IFeatureStyleProvider;

/**
 * Abstract feature-based (point and shape data) OGC Web Map Service plugin
 * implementation
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
public abstract class FeatureWmsSource<D extends SimpleDimension, L extends SimpleLayer<D>>
        extends AbstractWmsSource<D, L, PluginDataObject> implements
        IStyleLookupCallback<PluginDataObject> {

    protected FeatureFactory featureFactory;

    /**
     * @param props
     * @param key
     * @param layerTable
     * @param styles
     */
    public FeatureWmsSource(PluginProperties props, String key,
            LayerTransformer<D, L> transformer, FeatureFactory featureFactory) {
        super(props, key, transformer);
        this.featureFactory = featureFactory;
    }

    protected abstract List<SimpleFeature> getFeatures(String layer,
            GridGeometry2D geometry, String time, String elevation,
            Map<String, String> dimensions, double scale) throws WmsException;

    /**
     * @param layer
     * @return default styler if layer is null
     */
    protected abstract IFeatureStyleProvider getStyleProvider(String layer)
            throws WmsException;

    @Override
    public Collection<OgcStyle> getStyles() {
        IFeatureStyleProvider styler;
        try {
            styler = getStylerInternal(null);
        } catch (WmsException e) {
            log.error("Problem getting styler", e);
            return new ArrayList<OgcStyle>(0);
        }
        return styler.getStyles();
    }

    @Override
    protected StyleLookup getStyleLookup() throws WmsException {
        return getStylerInternal(null);
    }

    protected IFeatureStyleProvider getStylerInternal(String layer)
            throws WmsException {
        IFeatureStyleProvider rval = getStyleProvider(layer);
        return rval;
    }

    @Override
    public WmsImage getImage(String rawLayer, String style,
            boolean defaultStyle, GridGeometry2D targetGeom, String time,
            String elevation, Map<String, String> dimensions, double scale)
            throws WmsException {
        String layer = parseIncomingLayerName(rawLayer);
        List<SimpleFeature> features = getFeatures(layer, targetGeom, time,
                elevation, dimensions, scale);
        if (features.isEmpty()) {
            return null;
        }
        SimpleFeature sample = features.get(0);
        MemoryFeatureCollection coll = new MemoryFeatureCollection(
                sample.getFeatureType());
        coll.addAll(features);
        return getStylerInternal(layer).styleData(coll, layer, style,
                defaultStyle);
    }

    @Override
    public BufferedImage getLegend(String rawLayer, String style, String time,
            String elevation, Map<String, String> dimensions, Integer height,
            Integer width) throws WmsException {
        String layer = parseIncomingLayerName(rawLayer);
        IFeatureStyleProvider styler = getStyleProvider(layer);
        if (styler == null) {
            throw new WmsException(Code.LayerNotDefined);
        }
        return styler.getLegend(layer, style, dimensions,
                new HashMap<String, String>(0), width, height);
    }

}
