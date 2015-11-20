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

import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * Generates TileMatrixSet objects using scale sets
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
public class TileMatrixFactory {

    public static class ScaleSet {
        public double[] scales;

        public int[] matrixWidths;

        public int[] matrixHeights;

        public ScaleSet() {
            // TODO Auto-generated constructor stub
        }

        public ScaleSet(double[] scales, int[] matrixWidths, int[] matrixHeights) {
            this.scales = scales;
            this.matrixWidths = matrixWidths;
            this.matrixHeights = matrixHeights;
        }

    }

    public static TileMatrixSet createFixedDimSet(String id, String crs,
            ReferencedEnvelope bounds, int twidth, int theight, ScaleSet ss) {
        TileMatrix[] entries = new TileMatrix[ss.scales.length];
        for (int i = 0; i < entries.length; ++i) {
            entries[i] = new TileMatrix(i, ss.scales[i], bounds, twidth,
                    theight, ss.matrixWidths[i], ss.matrixHeights[i]);
        }
        return new TileMatrixSet(id, crs, entries, bounds);
    }

    public static String getMatrixId(String setId, TileMatrix matrix) {
        return setId + ":" + matrix.getIndex();
    }

    /**
     * @param set
     * @param matrixId
     * @return null if matrixId doesn't correspond to entry in set
     */
    public static TileMatrix getTileMatrix(TileMatrixSet set, String matrixId) {
        if (set == null || matrixId == null) {
            return null;
        }
        // index of entry should be only thing after last ':'
        int chrIndex = matrixId.lastIndexOf(":");
        if (chrIndex < 0) {
            // not a valid id
            return null;
        }
        // first part of matrix id is set id
        String setId = matrixId.substring(0, chrIndex);
        // confirm that the matrix is from this set
        if (!setId.equals(set.getIdentifier())) {
            return null;
        }
        String indexStr = matrixId.substring(chrIndex + 1);
        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (Throwable e) {
            // TODO should return more specific error
            return null;
        }
        TileMatrix[] entries = set.getMatrixEntries();
        if (index < 0 || index >= entries.length) {
            return null;
        }
        return entries[index];
    }
}
