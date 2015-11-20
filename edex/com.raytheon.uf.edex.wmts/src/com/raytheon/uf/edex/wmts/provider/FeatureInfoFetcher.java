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

import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsGetFeatureInfoRequest;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixRegistry;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;

/**
 * Utility to extract feature info from Web Map Tile Service sources
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
public class FeatureInfoFetcher extends BaseFetcher {

    /**
     * @param registry
     * @param sourceManager
     */
    public FeatureInfoFetcher(TileMatrixRegistry registry,
            WmtsSourceManager sourceManager) {
        super(registry, sourceManager);
    }

    public List<SimpleFeature> fetch(WmtsGetFeatureInfoRequest req)
            throws WmtsException {
        WmtsSource source = lookupSource(req);
        String lname = req.getLayer();
        WmtsLayer layer = source.getLayer(lname);
        TileMatrixSet mset = getMatrixSet(req);
        TileMatrix matrix = getMatrix(req, mset);
        fillWithDefaults(req, layer);

        return source.getFeatureInfo(layer, req.getDimensions(), req.gettRow(),
                req.gettCol(), mset, matrix, req.getI(), req.getJ());
    }

}
