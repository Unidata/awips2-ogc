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
package com.raytheon.uf.edex.wmts.tiling;

import java.util.HashMap;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.edex.ogc.common.OgcException;
import com.raytheon.uf.edex.ogc.common.spatial.CrsLookup;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixFactory.ScaleSet;

/**
 * Stores tile matrix sets associated with well known URNs
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
public class TileMatrixRegistry {

    public static String crs84TSetName = "CRS:84";

    public static String googleTSetName = "EPSG:900913";

    public static String crs84URN = "urn:ogc:def:crs:OGC::CRS84";

    public static String googleCrsURN = "urn:ogc:def:crs:EPSG::900913";

    protected TileMatrixSet crs84TileSet;

    protected TileMatrixSet googleTileSet;

    protected Map<String, TileMatrixSet> matrixMap = new HashMap<String, TileMatrixSet>();

    public TileMatrixRegistry() throws NoSuchAuthorityCodeException,
            OgcException, FactoryException {
        double[] crs84Scales = new double[] { 2.795411320143589E8,
                1.3977056600717944E8, 6.988528300358972E7, 3.494264150179486E7,
                1.747132075089743E7, 8735660.375448715, 4367830.1877243575,
                2183915.0938621787, 1091957.5469310894, 545978.7734655447,
                272989.38673277234, 136494.69336638617, 68247.34668319309,
                34123.67334159654, 17061.83667079827, 8530.918335399136,
                4265.459167699568, 2132.729583849784 };
        int[] crs84Widths = new int[] { 2, 4, 8, 16, 32, 64, 128, 256, 512,
                1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144 };
        int[] crs84Heights = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
                1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072 };
        ReferencedEnvelope crs84bounds = new ReferencedEnvelope(-180, 180, -90,
                90, MapUtil.LATLON_PROJECTION);
        crs84TileSet = TileMatrixFactory.createFixedDimSet(crs84TSetName,
                crs84URN, crs84bounds, 256, 256, new ScaleSet(crs84Scales,
                        crs84Widths, crs84Heights));
        double[] googleScales = new double[] { 5.590822639508929E8,
                2.7954113197544646E8, 1.3977056598772323E8,
                6.988528299386162E7, 3.494264149693081E7, 1.7471320748465404E7,
                8735660.374232702, 4367830.187116351, 2183915.0935581755,
                1091957.5467790877, 545978.7733895439, 272989.38669477194,
                136494.69334738597, 68247.34667369298, 34123.67333684649,
                17061.836668423246, 8530.918334211623, 4265.4591671058115 };
        int[] googleDims = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
                1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072 };
        CoordinateReferenceSystem googleCrs = CrsLookup.lookup(googleCrsURN);
        ReferencedEnvelope googleBounds = new ReferencedEnvelope(-20037508.34,
                20037508.34, -20037508.34, 20037508.34, googleCrs);
        googleTileSet = TileMatrixFactory.createFixedDimSet(googleTSetName,
                googleCrsURN, googleBounds, 256, 256, new ScaleSet(
                        googleScales, googleDims, googleDims));
        matrixMap.put(googleTileSet.getIdentifier(), googleTileSet);
        matrixMap.put(crs84TileSet.getIdentifier(), crs84TileSet);
    }

    public TileMatrixSet getTileMatrixSet(String id) {
        synchronized (matrixMap) {
            return matrixMap.get(id);
        }
    }

    public void register(TileMatrixSet set) throws Exception {
        if (set != null && set.getIdentifier() != null) {
            synchronized (matrixMap) {
                if (matrixMap.get(set.getIdentifier()) != null) {
                    throw new Exception(
                            "Tile Matrix already registered with id: "
                                    + set.getIdentifier());
                }
                matrixMap.put(set.getIdentifier(), set);
            }
        }
    }

    public void register(TileMatrixSource source) throws Exception {
        if (source != null) {
            for (TileMatrixSet set : source.getTileMatrixSets()) {
                synchronized (matrixMap) {
                    if (matrixMap.get(set.getIdentifier()) != null) {
                        // TODO should check all of them and not register any if
                        // there is a problem
                        throw new Exception(
                                "Tile Matrix already registered with id: "
                                        + set.getIdentifier());
                    }
                    matrixMap.put(set.getIdentifier(), set);
                }
            }
        }
    }
}
