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
package com.raytheon.uf.edex.wmts.statictile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ogc.common.OgcBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcDimension;
import com.raytheon.uf.edex.ogc.common.OgcGeoBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixFactory;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;
import org.locationtech.jts.geom.Envelope;

/**
 * Abstract class for Web Map Tile Service sources that use static
 * (pre-rendered) tiles
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
public abstract class StaticWmtsSource implements WmtsSource {

    protected static final IUFStatusHandler log = UFStatus
            .getHandler(StaticWmtsSource.class);

    /**
     * @param identifier
     * @param style
     * @param tileMatrixSet
     * @return
     * @throws UnsupportedEncodingException
     */
    protected WmtsLayer createLayer(String identifier, String style,
            String tileMatrixSet) throws UnsupportedEncodingException {
        // TODO fix assumption that all static data is global
        Envelope env = new Envelope(180, -180, 90, -90);
        OgcGeoBoundingBox geoBbox = new OgcGeoBoundingBox(env);
        List<OgcBoundingBox> bboxes = Arrays.asList(new OgcBoundingBox(
                "CRS:84", env));
        OgcStyle s = new OgcStyle(style, style);
        s.setDefault(true);
        List<OgcStyle> styles = Arrays.asList(s);
        List<OgcDimension> dims = new ArrayList<OgcDimension>(0);
        List<String> msets = new ArrayList<String>(1);
        msets.add(tileMatrixSet);
        return new WmtsLayer(URLEncoder.encode(identifier, "UTF-8"),
                identifier, null, geoBbox, styles, dims, msets, bboxes);
    }

    @Override
    public List<SimpleFeature> getFeatureInfo(WmtsLayer layer,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix, int i, int j) throws WmtsException {
        // TODO get data from tile?
        return new ArrayList<SimpleFeature>(0);
    }

    /**
     * Read image from static source
     * 
     * @param layer
     * @param style
     * @param dims
     * @param row
     * @param col
     * @param mset
     * @param matrix
     * @return
     * @throws WmtsException
     */
    abstract protected BufferedImage getImageInternal(WmtsLayer layer,
            String style, Map<String, String> dims, int row, int col,
            TileMatrixSet mset, TileMatrix matrix) throws WmtsException;

    @Override
    public BufferedImage getImage(WmtsLayer layer, String style,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix) throws WmtsException {
        BufferedImage rval = getImageInternal(layer, style, dims, row, col,
                mset, matrix);
        if (rval == null) {
            try {
                rval = generate(layer, style, dims, row, col, mset, matrix);
            } catch (Exception e) {
                log.error("Problem generating new tile", e);
                throw new WmtsException(Code.InternalServerError);
            }
        }
        return rval;
    }

    /**
     * Generate a tile in a matrix that does not exist in the static source
     * using the highest resolution matrix that exists in the source.
     * 
     * @param layer
     * @param style
     * @param dims
     * @param row
     * @param col
     * @param mset
     * @param matrix
     * @return
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws WmtsException
     */
    protected BufferedImage generate(WmtsLayer layer, String style,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix) throws MismatchedDimensionException,
            TransformException, WmtsException {
        TileMatrix onDisk = getHighestResolution(layer, style, mset);
        if (onDisk == null) {
            throw new WmtsException(Code.InternalServerError,
                    "Unable to generate image for layer "
                            + layer.getIdentifier() + " for set + "
                            + mset.getIdentifier() + ". No data found.");
        }
        ReferencedEnvelope bounds = mset.getBounds();

        MathTransform diskGridToCrs = createGridToCrs(mset, onDisk);
        MathTransform diskCrsToGrid = diskGridToCrs.inverse();
        MathTransform targetGridToCrs = createGridToCrs(mset, matrix);
        // target grid coordinate bounding target tile
        DirectPosition2D targetIndex = new DirectPosition2D(col, row);
        DirectPosition2D targetLowerRight = new DirectPosition2D(col + 1,
                row + 1);
        // convert grid coordinates to CRS
        DirectPosition2D crsTargetUL = new DirectPosition2D();
        DirectPosition2D crsTargetLR = new DirectPosition2D();
        targetGridToCrs.transform(targetIndex, crsTargetUL);
        targetGridToCrs.transform(targetLowerRight, crsTargetLR);
        // convert upper left CRS coord to index into tile on disk
        DirectPosition2D diskIndex = new DirectPosition2D();
        diskCrsToGrid.transform(crsTargetUL, diskIndex);
        int highCol = (int) Math.floor(diskIndex.x);
        int highRow = (int) Math.floor(diskIndex.y);
        // convert grid coordinates bounding tile on disk to CRS
        DirectPosition2D diskUL = new DirectPosition2D(highCol, highRow);
        DirectPosition2D diskLR = new DirectPosition2D(highCol + 1, highRow + 1);
        DirectPosition2D crsDiskUL = new DirectPosition2D();
        DirectPosition2D crsDiskLR = new DirectPosition2D();
        diskGridToCrs.transform(diskUL, crsDiskUL);
        diskGridToCrs.transform(diskLR, crsDiskLR);
        // use those grid coordinates to create a math transform to tile pixel
        // coordinates
        ReferencedEnvelope diskTileBounds = new ReferencedEnvelope(crsDiskLR.x,
                crsDiskUL.x, crsDiskLR.y, crsDiskUL.y,
                bounds.getCoordinateReferenceSystem());
        MathTransform tileCrsToGrid = createTileCrsToGrid(onDisk,
                diskTileBounds);

        DirectPosition2D tileUL = new DirectPosition2D();
        DirectPosition2D tileLR = new DirectPosition2D();
        tileCrsToGrid.transform(crsTargetUL, tileUL);
        tileCrsToGrid.transform(crsTargetLR, tileLR);
        // pixel coordinates in tile on disk
        int startX = (int) Math.round(tileUL.x);
        int startY = (int) Math.round(tileUL.y);
        int endX = (int) Math.round(tileLR.x);
        int endY = (int) Math.round(tileLR.y);
        // get tile from disk
        BufferedImage disk = getImageInternal(layer, style, dims, highRow,
                highCol, mset, onDisk);
        if (disk == null) {
            throw new WmtsException(Code.InternalServerError,
                    "Unable to generate image for layer "
                            + layer.getIdentifier() + " for set + "
                            + mset.getIdentifier() + " matrix number "
                            + onDisk.getIndex() + ". Missing tile (col " + col
                            + ", row " + row + ")");
        }
        // create new tile
        BufferedImage sub = new BufferedImage(matrix.getTileWidth(),
                matrix.getTileHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sub.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(disk, 0, 0, sub.getWidth(), sub.getHeight(), startX,
                startY, endX, endY, null);
        g.dispose();

        return sub;
    }

    /**
     * Create a math transform to go from matrix grid coordinates to CRS
     * coordinates
     * 
     * @param mset
     * @param matrix
     * @return
     */
    protected MathTransform createGridToCrs(TileMatrixSet mset,
            TileMatrix matrix) {
        ReferencedEnvelope bounds = mset.getBounds();
        GridGeometry2D gg = new GridGeometry2D(new GridEnvelope2D(0, 0,
                matrix.getMatrixWidth(), matrix.getMatrixHeight()), bounds);
        return gg.getGridToCRS(PixelOrientation.UPPER_LEFT);
    }

    /**
     * Create a math transform to go from CRS coordinates to tile (pixel)
     * coordinates.
     * 
     * @param matrix
     * @param bounds
     * @return
     */
    protected MathTransform createTileCrsToGrid(TileMatrix matrix,
            ReferencedEnvelope bounds) {
        GridGeometry2D tileGG = new GridGeometry2D(new GridEnvelope2D(0, 0,
                matrix.getTileWidth(), matrix.getTileHeight()), bounds);
        return tileGG.getCRSToGrid2D(PixelOrientation.UPPER_LEFT);
    }

    /**
     * @param layer
     * @param style
     * @param mset
     * @return the highest resolution matrix that exists in the static source
     * @throws WmtsException
     */
    abstract protected TileMatrix getHighestResolution(WmtsLayer layer,
            String style, TileMatrixSet mset) throws WmtsException;

    @Override
    public WmtsLayer getLayer(String identifier) throws WmtsException {
        String normalized;
        try {
            // decode first to normalize, then encode to match id
            normalized = URLDecoder.decode(identifier, "UTF-8");
            normalized = URLEncoder.encode(normalized, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("problem decoding id", e);
            return null;
        }
        for (WmtsLayer l : listLayers()) {
            if (l.getIdentifier().equals(normalized)) {
                return l;
            }
        }
        return null;
    }

    protected String getTileKey(WmtsLayer layer, String style,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix) {
        // format is 'layer-style-matrixset/matrix/dims-col-row'
        StringBuilder sb = new StringBuilder();
        sb.append(getMatrixSetDir(layer, style, mset));
        sb.append(File.separatorChar);
        sb.append(getMatrixDir(mset, matrix)).append(File.separatorChar);
        appendDimKey(sb, layer, dims);
        sb.append("col").append(col).append('-');
        sb.append("row").append(row);
        return sb.toString();
    }

    protected String getMatrixSetDir(WmtsLayer layer, String style,
            TileMatrixSet mset) {
        StringBuilder sb = new StringBuilder();
        sb.append(sanitize(layer.getTitle())).append('-');
        sb.append(sanitize(style)).append('-');
        sb.append(sanitize(mset.getIdentifier()));
        return sb.toString();
    }

    protected String getMatrixDir(TileMatrixSet mset, TileMatrix matrix) {
        String matrixName = TileMatrixFactory.getMatrixId(mset.getIdentifier(),
                matrix);
        return sanitize(matrixName);
    }

    /**
     * @param sb
     * @param layer
     * @param req
     */
    private void appendDimKey(StringBuilder sb, WmtsLayer layer,
            Map<String, String> reqDims) {
        Collection<OgcDimension> layerDims = layer.getDimensions();
        // ensure dimension order
        TreeSet<String> dimset = new TreeSet<String>();
        for (OgcDimension od : layerDims) {
            dimset.add(od.getName());
        }
        if (dimset.isEmpty()) {
            return;
        }
        Iterator<String> i = dimset.iterator();
        sb.append(reqDims.get(i.next()));
        while (i.hasNext()) {
            sb.append('-');
            sb.append(sanitize(reqDims.get(i.next())));
        }
        sb.append('-');
    }

    /**
     * Replace file system special chars with '-'
     * 
     * @param s
     * @return
     */
    private String sanitize(String s) {
        if (s == null) {
            return "null";
        }
        String rval = StringUtils.replaceChars(s, "./\\?%*:|\"<>",
                "-----------");
        rval = StringUtils.chomp(rval, "-");
        return rval.substring(StringUtils.indexOfAnyBut(rval, "-"));
    }

}
