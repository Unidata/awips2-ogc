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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.raytheon.uf.edex.ogc.common.OgcLayer;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;

/**
 * Web Map Tile Service source that gets static tiles from the file system
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
public class FileStaticWmtsSource extends StaticWmtsSource {

    protected File baseDir;

    protected static Pattern topLevelPattern = Pattern
            .compile("^([^-]+)-([^-]+)-([^-]+)-(.*)$");

    /**
	 * 
	 */
    public FileStaticWmtsSource(File baseDirectory) {
        this.baseDir = baseDirectory;
    }

    @Override
    public List<WmtsLayer> listLayers() {
        // TODO cache
        Map<String, WmtsLayer> layerMap = new HashMap<String, WmtsLayer>();
        for (File f : baseDir.listFiles()) {
            if (!f.isDirectory()) {
                continue;
            }
            Matcher m = topLevelPattern.matcher(f.getName());
            if (m.matches()) {
                String key = m.group(1);
                String name = m.group(2);
                String style = m.group(3);
                String mset = m.group(4).replaceAll("-", ":");
                String id = key + OgcLayer.keySeparator + name;
                WmtsLayer layer = layerMap.get(id);
                if (layer == null) {
                    try {
                        layer = createLayer(id, style, mset);
                        layerMap.put(id, layer);
                    } catch (UnsupportedEncodingException e) {
                        log.error("Problem encoding identifier", e);
                    }
                } else {
                    // FIXME this assumes only matrix set is different, could be
                    // a different style
                    layer.getTileMatrixSets().add(mset);
                }
            }
        }
        return new ArrayList<WmtsLayer>(layerMap.values());
    }

    @Override
    protected BufferedImage getImageInternal(WmtsLayer layer, String style,
            Map<String, String> dims, int row, int col, TileMatrixSet mset,
            TileMatrix matrix) throws WmtsException {
        String tileKey = getTileKey(layer, style, dims, row, col, mset, matrix);
        BufferedImage rval = null;
        File f = new File(baseDir, tileKey);
        if (f.exists()) {
            if (!f.canRead()) {
                log.error("Unable to read tile from static tile set: " + f);
                throw new WmtsException(Code.InternalServerError);
            }
            try {
                rval = ImageIO.read(f);
            } catch (IOException e) {
                log.error("Problem reading static data", e);
                throw new WmtsException(Code.InternalServerError);
            }
        }
        return rval;
    }

    @Override
    protected TileMatrix getHighestResolution(WmtsLayer layer, String style,
            TileMatrixSet mset) {
        File setDir = new File(baseDir, getMatrixSetDir(layer, style, mset));
        TileMatrix rval = null;
        for (TileMatrix m : mset.getMatrixEntries()) {
            File matrixDir = new File(setDir, getMatrixDir(mset, m));
            if (matrixDir.isDirectory()) {
                rval = m;
            } else {
                break;
            }
        }
        return rval;
    }

    /**
     * @return the baseDir
     */
    public File getBaseDir() {
        return baseDir;
    }

    @Override
    public String getKey() {
        return baseDir.getName();
    }

}
