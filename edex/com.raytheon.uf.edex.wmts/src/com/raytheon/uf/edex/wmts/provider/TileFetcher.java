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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.edex.ogc.common.OgcDimension;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;
import com.raytheon.uf.edex.wmts.GetTileRequest;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.cache.TileCacheManager;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.statictile.StaticWmtsSource;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixRegistry;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;

/**
 * Utility for extracting tiles from Web Map Tile Service sources
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
public class TileFetcher extends BaseFetcher {

    protected TileCacheManager cache;

    /**
     * @param registry
     * @param sourceManager
     */
    public TileFetcher(TileMatrixRegistry registry,
            WmtsSourceManager sourceManager, TileCacheManager cache) {
        super(registry, sourceManager);
        this.cache = cache;
    }

    /**
     * Send requested tile as response. This uses a cache.
     * 
     * @param req
     * @param response
     * @throws WmtsException
     */
    public void fulfill(GetTileRequest req, IOgcHttpResponse response)
            throws WmtsException {
        WmtsSource source = lookupSource(req);
        WmtsLayer layer = source.getLayer(req.getLayer());
        if (layer == null) {
            throw new WmtsException(Code.LayerNotDefined);
        }
        fillWithDefaults(req, layer);
        String tileKey = getTileKey(layer, req);

        if (source instanceof StaticWmtsSource) {
            /* static sources are almost like caches that don't expire */
            fulfillDirect(source, layer, req, response);
        } else {
            if (!fulfillFromCache(req, tileKey, response)) {
                // cache miss
                BufferedImage tile = fulfillDirect(source, layer, req, response);
                cache.putTile(tileKey, tile);
            }
        }
    }

    private final BufferedImage fulfillDirect(WmtsSource source,
            WmtsLayer layer, GetTileRequest req, IOgcHttpResponse response)
            throws WmtsException {
        TileMatrixSet mset = getMatrixSet(req);
        TileMatrix matrix = getMatrix(req, mset);
        BufferedImage tile = source
                .getImage(layer, req.getStyle(), req.getDimensions(),
                        req.gettRow(), req.gettCol(), mset, matrix);
        writeImage(req, tile, response);
        return tile;
    }

    protected boolean fulfillFromCache(GetTileRequest req, String tileKey,
            IOgcHttpResponse response) throws WmtsException {
        boolean foundInCache = false;
        byte[] tile = cache.getTile(tileKey, req.getFormat());
        if (tile != null) {
            foundInCache = true;
            response.setContentType(req.getFormat().toString());
            try (OutputStream out = response.getOutputStream()) {
                out.write(tile);
            } catch (IOException e) {
                throw new WmtsException(Code.InternalServerError,
                        "Unable to write tile to stream", e);
            }
        }
        return foundInCache;
    }

    /**
     * Build tile key using request parameters.
     * 
     * @param layer
     * @param req
     * @return
     */
    protected String getTileKey(WmtsLayer layer, GetTileRequest req) {
        // format is 'layer-style-matrixset/matrix/dims-col-row'
        StringBuilder sb = new StringBuilder();
        sb.append(sanitize(req.getLayer())).append('-');
        sb.append(sanitize(req.getStyle())).append('-');
        sb.append(sanitize(req.gettMatrixSet())).append(File.separatorChar);
        sb.append(sanitize(req.gettMatrix())).append(File.separatorChar);
        appendDimKey(sb, layer, req);
        sb.append("col").append(req.gettCol()).append('-');
        sb.append("row").append(req.gettRow());
        return sb.toString();
    }

    /**
     * @param sb
     * @param layer
     * @param req
     */
    private void appendDimKey(StringBuilder sb, WmtsLayer layer,
            GetTileRequest req) {
        Map<String, String> reqDims = req.getDimensions();
        Collection<OgcDimension> layerDims = layer.getDimensions();
        if (layerDims != null && !layerDims.isEmpty()) {
            // ensure time and elevation are in map (possibly redundant)
            reqDims.put(TIME_KEY, req.getTime());
            reqDims.put(ELEV_KEY, req.getElevation());
            // ensure dimension order
            TreeSet<String> dimset = new TreeSet<String>();
            for (OgcDimension od : layerDims) {
                dimset.add(od.getName());
            }
            Iterator<String> i = dimset.iterator();
            sb.append(reqDims.get(i.next()));
            while (i.hasNext()) {
                sb.append('-');
                sb.append(sanitize(reqDims.get(i.next())));
            }
            sb.append('-');
        }
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

    /**
     * @param req
     * @param tile
     * @throws WmtsException
     */
    protected void writeImage(GetTileRequest req, BufferedImage tile,
            IOgcHttpResponse response) throws WmtsException {
        MimeType mimetype = req.getFormat();
        Iterator<?> it = ImageIO.getImageWritersByMIMEType(mimetype
                .toStringWithoutParams());
        if (!it.hasNext()) {
            throw new WmtsException(Code.InvalidParameterValue,
                    "Format not supported: " + mimetype);
        }
        try {
            ImageWriter writer = (ImageWriter) it.next();
            response.setContentType(mimetype.toString());
            ImageOutputStream out = ImageIO.createImageOutputStream(response
                    .getOutputStream());
            writer.setOutput(out);
            writer.write(tile);
            out.close();
        } catch (Exception e) {
            log.error("Unable to output image", e);
        }
    }

}
