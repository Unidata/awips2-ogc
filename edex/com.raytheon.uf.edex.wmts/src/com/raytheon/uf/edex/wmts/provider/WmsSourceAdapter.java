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
package com.raytheon.uf.edex.wmts.provider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ogc.common.OgcLayer;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer;
import com.raytheon.uf.edex.ogc.common.spatial.CrsLookup;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.provider.OgcWmsProvider;
import com.raytheon.uf.edex.wms.reg.WmsImage;
import com.raytheon.uf.edex.wms.reg.WmsSource;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Web Map Tile Service source that is backed by a Web Map Service source
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2012                    bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class WmsSourceAdapter implements WmtsSource {

    protected static final String TIME_KEY = BaseFetcher.TIME_KEY;

    protected static final String ELEV_KEY = BaseFetcher.ELEV_KEY;

    protected WmsSource source;

    protected IUFStatusHandler log = UFStatus.getHandler(this.getClass());

    /**
	 * 
	 */
    public WmsSourceAdapter(WmsSource source) {
        this.source = source;
    }

    @Override
    public List<WmtsLayer> listLayers() {
        List<WmtsLayer> rval = new ArrayList<WmtsLayer>();
        for (OgcLayer layer : flatten(source.listLayers())) {
            rval.add(new WmtsLayer(layer));
        }
        return rval;
    }

    /**
     * Pull out leaf layer nodes as flat list
     * 
     * @param layers
     * @return
     */
    protected Collection<OgcLayer> flatten(Collection<OgcLayer> layers) {
        List<OgcLayer> rval = new ArrayList<OgcLayer>();
        for (OgcLayer layer : layers) {
            flatten(layer, rval, new HashMap<String, OgcStyle>());
        }
        return rval;
    }

    /**
     * Recursive method to get leaf nodes as flat list
     * 
     * @param layer
     * @param rval
     */
    protected void flatten(OgcLayer layer, List<OgcLayer> rval,
            HashMap<String, OgcStyle> styles) {
        Collection<OgcLayer> children = layer.getChildren();
        if (children == null || children.isEmpty()) {
            // leaf node, add if it has data
            addLeaf(layer, rval, styles);
        } else {
            // parent node, recurse
            HashMap<String, OgcStyle> nextStyles = mergeStyles(layer, styles);
            for (OgcLayer child : children) {
                flatten(child, rval, nextStyles);
            }
        }
    }

    protected void addLeaf(OgcLayer layer, List<OgcLayer> rval,
            HashMap<String, OgcStyle> styles) {
        // TODO grab inherited parameters from parents
        String name = layer.getName();
        Collection<OgcStyle> lstyles = layer.getStyles();
        if (name != null && !name.isEmpty()) {
            if (lstyles != null && !lstyles.isEmpty()) {
                layer.setStyles(createLayerStyles(name, lstyles, styles));
            }
            rval.add(layer);
        }
    }

    protected List<OgcStyle> createLayerStyles(String lName,
            Collection<OgcStyle> childStyles,
            HashMap<String, OgcStyle> parentStyles) {
        HashMap<String, OgcStyle> all = new LinkedHashMap<String, OgcStyle>();
        for (OgcStyle style : childStyles) {
            String sName = style.getName();
            if (style.getLegendUrl() == null) {
                String url = LayerTransformer.createLegendUrl(lName, sName);
                style.setLegendUrl(url);
            }
            all.put(sName, style);
        }
        for (String sName : parentStyles.keySet()) {
            if (all.containsKey(sName)) {
                continue;
            }
            OgcStyle style = parentStyles.get(sName);
            // create a copy since we are using the layername to build the url
            OgcStyle copy = new OgcStyle(style.getName(), style.getTitle());
            copy.setAbs(style.getAbs());
            String url = LayerTransformer.createLegendUrl(lName, sName);
            copy.setLegendUrl(url);
            all.put(sName, copy);
        }
        return new ArrayList<OgcStyle>(all.values());
    }

    protected HashMap<String, OgcStyle> mergeStyles(OgcLayer layer,
            HashMap<String, OgcStyle> styles) {
        Collection<OgcStyle> lstyles = layer.getStyles();
        HashMap<String, OgcStyle> nextStyles = new HashMap<String, OgcStyle>();
        nextStyles.putAll(styles);
        if (lstyles != null) {
            for (OgcStyle s : lstyles) {
                nextStyles.put(s.getName(), s);
            }
        }
        return nextStyles;
    }

    @Override
    public List<SimpleFeature> getFeatureInfo(WmtsLayer layer,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix, int i, int j) throws WmtsException {

        GridGeometry2D geom = getGeometry(row, col, mset, matrix);
        Coordinate c = getCoordinate(i, j, geom);
        double scale = OgcWmsProvider.getScale(geom);
        try {
            String time = dims.get(TIME_KEY);
            String elevation = dims.get(ELEV_KEY);
            return source.getFeatureInfo(layer.getIdentifier(), geom, time,
                    elevation, dims, c, scale);
        } catch (WmsException e) {
            throw new WmtsException(e);
        }
    }

    protected Coordinate getCoordinate(int i, int j, GridGeometry2D geom)
            throws WmtsException {
        try {
            return OgcWmsProvider.getCrsCoord(geom, i, j);
        } catch (Throwable e) {
            log.error("Problem getting CRS coordinates", e);
            throw new WmtsException(Code.InternalServerError);
        }
    }

    /**
     * Look up CRS object based on crs of the tile matrix set
     * 
     * @param mset
     * @return
     * @throws WmtsException
     *             InternalServerError if lookup fails
     */
    protected CoordinateReferenceSystem getCrs(TileMatrixSet mset)
            throws WmtsException {
        try {
            CoordinateReferenceSystem rval = CrsLookup.lookup(mset
                    .getSupportedCrs());
            if (rval == null) {
                // since the tile matrix set came from the registry, we can
                // assume that any error is our fault and not the client
                throw new Exception("Crs lookup return val was null");
            }
            return rval;
        } catch (Exception e) {
            log.error("Problem looking up crs", e);
            throw new WmtsException(Code.InternalServerError);

        }
    }

    protected GridGeometry2D getGeometry(int row, int col, TileMatrixSet mset,
            TileMatrix matrix) throws WmtsException {
        CoordinateReferenceSystem crs = getCrs(mset);
        Envelope bounds = getBounds(row, col, mset, matrix);
        ReferencedEnvelope env = new ReferencedEnvelope(bounds, crs);
        GridEnvelope2D range = new GridEnvelope2D(0, 0, matrix.getTileWidth(),
                matrix.getTileHeight());
        return new GridGeometry2D(range, env);
    }

    /**
     * Ensure that i is between 0 and max inclusive
     * 
     * @param i
     * @param max
     * @param label
     * @throws WmtsException
     *             TileOutOfRange if i is out of bounds
     */
    protected void validate(Integer i, int max, String label)
            throws WmtsException {
        if (i < 0 || i > max) {
            throw new WmtsException(Code.TileOutOfRange, "Invalid " + label
                    + ": " + i);
        }
    }

    /**
     * Calculate envelope for tile specified by request object
     * 
     * @param req
     * @param mset
     * @param matrix
     * @return
     * @throws WmtsException
     *             TileOutOfRange if request is out of range
     */
    protected Envelope getBounds(int row, int col, TileMatrixSet mset,
            TileMatrix matrix) throws WmtsException {
        validate(row, matrix.getMatrixHeight() - 1, "row");
        validate(col, matrix.getMatrixWidth() - 1, "column");
        // TODO this should be cached
        GridGeometry2D geom = new GridGeometry2D(new GridEnvelope2D(0, 0,
                matrix.getMatrixWidth(), matrix.getMatrixHeight()),
                mset.getBounds());
        MathTransform gridToCRS = geom
                .getGridToCRS(PixelOrientation.UPPER_LEFT);
        DirectPosition2D gridUL = new DirectPosition2D(col, row);
        DirectPosition2D gridLR = new DirectPosition2D(col + 1, row + 1);
        DirectPosition2D crsUL = new DirectPosition2D();
        DirectPosition2D crsLR = new DirectPosition2D();
        try {
            gridToCRS.transform(gridUL, crsUL);
            gridToCRS.transform(gridLR, crsLR);
        } catch (Exception e) {
            log.error("Problem getting bounds for tile", e);
            throw new WmtsException(Code.InternalServerError);
        }
        return new Envelope(crsLR.x, crsUL.x, crsLR.y, crsUL.y);
    }

    @Override
    public WmtsLayer getLayer(String identifier) throws WmtsException {
        try {
            OgcLayer l = source.getLayer(identifier);
            if (l == null) {
                throw new WmtsException(Code.LayerNotDefined);
            }
            return new WmtsLayer(l);
        } catch (WmsException e) {
            throw new WmtsException(e);
        }
    }

    @Override
    public BufferedImage getImage(WmtsLayer layer, String style,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix) throws WmtsException {
        GridGeometry2D geom = getGeometry(row, col, mset, matrix);
        WmsImage image = getImage(layer.getIdentifier(), style, dims, mset,
                matrix, geom, source);
        try {
            return OgcWmsProvider.mergeWmsImages(Arrays.asList(image), true,
                    null, geom);
        } catch (WmsException e) {
            throw new WmtsException(e);
        }
    }

    protected WmsImage getImage(String name, String style,
            Map<String, String> dims, TileMatrixSet mset, TileMatrix matrix,
            GridGeometry2D geom, WmsSource source) throws WmtsException {
        if (style != null && style.equalsIgnoreCase("default")) {
            // TODO tie this into the capabilities
            style = null;
        }
        String time = dims.get(TIME_KEY);
        String elevation = dims.get(ELEV_KEY);
        double scale = OgcWmsProvider.getScale(geom);
        try {
            WmsImage rval = source.getImage(name, style, style == null, geom,
                    time, elevation, dims, scale);
            if (rval == null) {
                rval = new WmsImage((GridCoverage2D) null);
            }
            return rval;
        } catch (WmsException e) {
            throw new WmtsException(e);
        }
    }

    @Override
    public String getKey() {
        return source.getKey();
    }

}
