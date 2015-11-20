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
 * Represents a 2D array of tiles
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
public class TileMatrix {

    protected int index;

    protected double scaleDenominator;

    protected ReferencedEnvelope bounds;

    protected int tileWidth;

    protected int tileHeight;

    protected int matrixWidth;

    protected int matrixHeight;

    public TileMatrix(int index, double scaleDenominator,
            ReferencedEnvelope bounds, int tileWidth, int tileHeight,
            int matrixWidth, int matrixHeight) {
        this.index = index;
        this.scaleDenominator = scaleDenominator;
        this.bounds = bounds;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.matrixWidth = matrixWidth;
        this.matrixHeight = matrixHeight;
    }

    public int getIndex() {
        return index;
    }

    public double getScaleDenominator() {
        return scaleDenominator;
    }

    /**
     * @return the bounds
     */
    public ReferencedEnvelope getBounds() {
        return bounds;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

}
