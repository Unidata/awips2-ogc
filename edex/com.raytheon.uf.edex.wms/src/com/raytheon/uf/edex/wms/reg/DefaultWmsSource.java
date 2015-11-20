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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.PluginProperties;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.edex.ogc.common.OgcNamespace;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.StyleLookup;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer;
import com.raytheon.uf.edex.ogc.common.db.SimpleDimension;
import com.raytheon.uf.edex.ogc.common.db.SimpleLayer;
import com.raytheon.uf.edex.ogc.common.reprojection.RecordReprojector;
import com.raytheon.uf.edex.ogc.common.reprojection.ReferencedDataRecord;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;
import com.raytheon.uf.edex.wms.styling.ICoverageStyleProvider;
import com.raytheon.uf.edex.wms.styling.IWmsDataRetriever;
import com.raytheon.uf.edex.wms.styling.WmsStyleChoice;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Abstract coverage-based (gridded data) OGC Web Map Service plugin
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
public abstract class DefaultWmsSource<D extends SimpleDimension, L extends SimpleLayer<D>, R extends PluginDataObject>
        extends AbstractWmsSource<D, L, R> implements IWmsDataRetriever {

    public DefaultWmsSource(PluginProperties props, String key,
            LayerTransformer<D, L> transformer) {
        super(props, key, transformer);
    }

    @Override
    public List<SimpleFeature> getFeatureInfo(String rawLayer,
            GridGeometry2D targetGeom, String time, String elevation,
            Map<String, String> dimensions, Coordinate c, double scale)
            throws WmsException {
        String layer = parseIncomingLayerName(rawLayer);
        R record = getRecord(layer, time, elevation, dimensions);
        double value;
        try {
            CoordinateReferenceSystem crs = targetGeom
                    .getCoordinateReferenceSystem();
            value = getRecordProjector(record).getDatastoreValue(crs, c, 0);
            ICoverageStyleProvider<R> styleProvider = getStyleProvider(rawLayer);
            value = styleProvider.convertToDisplay(rawLayer, record, value);
        } catch (Exception e) {
            log.error("Problem retrieving feature data", e);
            throw new WmsException(Code.InternalServerError);
        }
        return Arrays.asList(wrapInFeature(layer, value, record));
    }

    protected SimpleFeature wrapInFeature(String name, double value,
            PluginDataObject record) {
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName(key);
        tbuilder.setNamespaceURI(OgcNamespace.EDEX);
        tbuilder.add("value", Double.class);
        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(type);
        fbuilder.add(value);
        return fbuilder.buildFeature(name);
    }

    @Override
    public WmsImage getImage(String rawLayer, String style,
            boolean defaultStyle, GridGeometry2D geom, String time,
            String elevation, Map<String, String> dimensions, double scale)
            throws WmsException {
        WmsImage rval;
        String layer = parseIncomingLayerName(rawLayer);
        R record = getRecord(layer, time, elevation, dimensions);
        ICoverageStyleProvider<R> styler = getStylerInternal(layer);
        WmsStyleChoice choice;
        if (!defaultStyle && style == null) {
            // return a coverage and let them handle the style
            choice = new WmsStyleChoice((Style) null);
        } else {
            // set style to null so we dont try to load an empty string as a
            // colormap name
            if (defaultStyle) {
                style = null;
            }
            choice = styler.getStyle(layer, record, style);
        }
        rval = styler.styleData(this, choice, record, geom);

        return rval;
    }

    /**
     * @param layer
     * @return default style provider if layer is null
     * @throws WmsException
     */
    protected abstract ICoverageStyleProvider<R> getStyleProvider(String layer)
            throws WmsException;

    @Override
    protected StyleLookup getStyleLookup() throws WmsException {
        return getStylerInternal(null);
    }

    protected ICoverageStyleProvider<R> getStylerInternal(String layer)
            throws WmsException {
        ICoverageStyleProvider<R> rval = getStyleProvider(layer);
        return rval;
    }

    @Override
    public Collection<OgcStyle> getStyles() {
        ICoverageStyleProvider<R> styler;
        try {
            styler = getStylerInternal(null);
        } catch (WmsException e) {
            log.error("Problem getting style provider", e);
            return new ArrayList<OgcStyle>(0);
        }
        return styler.getStyles();
    }

    @Override
    public ReferencedDataRecord getDataRecord(PluginDataObject record,
            ReferencedEnvelope envelope) throws WmsException {
        try {
            return getRecordProjector(record).getProjected(envelope);
        } catch (Exception e) {
            log.error("Unable to get reprojected data for record: " + record, e);
            throw new WmsException(Code.InternalServerError);
        }
    }

    @Override
    public GridCoverage2D getGridCoverage(PluginDataObject record,
            ReferencedEnvelope envelope) throws WmsException {
        try {
            return getRecordProjector(record).getProjectedCoverage(envelope);
        } catch (Exception e) {
            log.error("Unable to get reprojected data for record: " + record, e);
            throw new WmsException(Code.InternalServerError);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.wms.reg.WmsSource#getLegend(java.lang.String,
     * java.lang.String, boolean, java.lang.String, java.lang.String,
     * java.util.Map, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public BufferedImage getLegend(String rawLayer, String style, String time,
            String elevation, Map<String, String> dimensions, Integer height,
            Integer width) throws WmsException {
        String layer = parseIncomingLayerName(rawLayer);
        R record = getRecord(layer, time, elevation, dimensions);
        ICoverageStyleProvider<R> styler = getStylerInternal(layer);
        if (styler == null) {
            throw new WmsException(Code.LayerNotDefined);
        }
        return styler.getLegend(layer, record, style, width, height);
    }

    /**
     * Get the record projector for the given record.
     * 
     * @param record
     *            the record to get a projector for
     * @return a new instance of the record projector to use for the given
     *         record.
     * @throws PluginException
     *             if the dao for the record could not be obtained
     */
    protected RecordReprojector getRecordProjector(PluginDataObject record)
            throws PluginException {
        return new RecordReprojector(record, getDao().getDataStore(
                (IPersistable) record));
    }

}
