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
package com.raytheon.uf.edex.wmts.reg;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.opengis.feature.simple.SimpleFeature;

import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixSet;

/**
 * Interface for providing image tiles to the Web Map Tile Service
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
public interface WmtsSource {

    public List<WmtsLayer> listLayers() throws WmtsException;

	public List<SimpleFeature> getFeatureInfo(WmtsLayer layer,
			Map<String, String> dims, int row, int col, TileMatrixSet mset,
			TileMatrix matrix, int i, int j) throws WmtsException;

	public BufferedImage getImage(WmtsLayer layer, String style,
			Map<String, String> dims, int row, int col, TileMatrixSet mset,
			TileMatrix matrix) throws WmtsException;

	public WmtsLayer getLayer(String identifier) throws WmtsException;

	public String getKey();

}
