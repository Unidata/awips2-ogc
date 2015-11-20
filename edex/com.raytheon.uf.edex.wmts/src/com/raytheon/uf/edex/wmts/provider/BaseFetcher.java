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

import java.util.Collection;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ogc.common.OgcDimension;
import com.raytheon.uf.edex.wmts.GetTileRequest;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixFactory;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixRegistry;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;

/**
 * Abstract class for Web Map Tile Service utilities that fulfill internal
 * operation requests
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
public abstract class BaseFetcher {

    public static final String TIME_KEY = "time";

    public static final String ELEV_KEY = "elevation";

    protected TileMatrixRegistry registry;

    protected WmtsSourceManager sourceManager;

    protected IUFStatusHandler log = UFStatus.getHandler(this.getClass());

    /**
     * @param registry
     * @param sourceManager
     */
    public BaseFetcher(TileMatrixRegistry registry,
            WmtsSourceManager sourceManager) {
        this.registry = registry;
        this.sourceManager = sourceManager;
    }

    /**
     * Query registry for tile matrix set specified in request
     * 
     * @param req
     *            request object that must have a non-null tile matrix set field
     * @return
     * @throws WmtsException
     *             InvalidParameterValue if the matrix set is not found in
     *             registry
     */
    protected TileMatrixSet getMatrixSet(GetTileRequest req)
            throws WmtsException {
        String msetName = req.gettMatrixSet();
        TileMatrixSet mset = registry.getTileMatrixSet(msetName);
        if (mset == null) {
            throw new WmtsException(Code.InvalidParameterValue,
                    "Unknown tile matrix set: " + msetName);
        }
        return mset;
    }

    /**
     * Extracts tile matrix from tile matrix set
     * 
     * @param req
     *            request object that has a non-null tile matrix field
     * @param mset
     * @return
     * @throws WmtsException
     *             InvalidParameterValue if the matrix is not valid for the set
     */
    protected TileMatrix getMatrix(GetTileRequest req, TileMatrixSet mset)
            throws WmtsException {
        String matrixId = req.gettMatrix();
        TileMatrix matrix = TileMatrixFactory.getTileMatrix(mset, matrixId);
        if (matrix == null) {
            throw new WmtsException(Code.InvalidParameterValue,
                    "Invalid tile matrix for requested set: " + matrixId);
        }
        return matrix;
    }

    /**
     * Get wms source for layer specified in request object
     * 
     * @param req
     * @return
     * @throws WmtsException
     *             LayerNotDefined if no source is found for layer name
     */
    protected WmtsSource lookupSource(GetTileRequest req) throws WmtsException {
        WmtsSource source = sourceManager.getSource(req.getLayer());
        if (source == null) {
            throw new WmtsException(Code.LayerNotDefined);
        }
        return source;
    }

    /**
     * Ensure that request has all values. Any missing values are filled with
     * defaults from layer object.
     * 
     * @param req
     * @param layer
     */
    protected void fillWithDefaults(GetTileRequest req, WmtsLayer layer) {
        Map<String, String> dims = req.getDimensions();
        String time = req.getTime();
        if (time != null && !time.equalsIgnoreCase("current")) {
            dims.put(TIME_KEY, time);
        }
        dims.put(ELEV_KEY, req.getElevation());
        Collection<OgcDimension> dimList = layer.getDimensions();
        if (dimList != null) {
            for (OgcDimension dim : dimList) {
                String name = dim.getName();
                String value = dims.get(name);
                if (value == null) {
                    dims.put(name, dim.getDefaultVal());
                }
            }
        }
        req.setTime(dims.get(TIME_KEY));
        req.setElevation(dims.get(ELEV_KEY));
    }

}
