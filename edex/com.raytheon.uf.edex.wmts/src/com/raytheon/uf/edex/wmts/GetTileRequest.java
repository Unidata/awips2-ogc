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
package com.raytheon.uf.edex.wmts;

import java.util.Map;

import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;

/**
 * Primary request object for Web Map Tile Service
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
public class GetTileRequest extends WmtsBaseRequest {

    protected String layer;

    protected String style;

    protected String tMatrixSet;

    protected String tMatrix;

    protected Integer tRow;

    protected Integer tCol;

    protected String time;

    protected String elevation;

    protected Map<String, String> dimensions;

    public GetTileRequest(String layer, String style, String tMatrixSet,
            String tMatrix, Integer tRow, Integer tCol, String time,
            String elevation, Map<String, String> dimensions) {
        this.layer = layer;
        this.style = style;
        this.tMatrixSet = tMatrixSet;
        this.tMatrix = tMatrix;
        this.tRow = tRow;
        this.tCol = tCol;
        this.time = time;
        this.elevation = elevation;
        this.dimensions = dimensions;
    }

    @Override
    public void execute(WmtsProvider provider, IOgcHttpResponse response) {
        provider.getTile(this, response);
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String gettMatrixSet() {
        return tMatrixSet;
    }

    public void settMatrixSet(String tMatrixSet) {
        this.tMatrixSet = tMatrixSet;
    }

    public String gettMatrix() {
        return tMatrix;
    }

    public void settMatrix(String tMatrix) {
        this.tMatrix = tMatrix;
    }

    public Integer gettRow() {
        return tRow;
    }

    public void settRow(Integer tRow) {
        this.tRow = tRow;
    }

    public Integer gettCol() {
        return tCol;
    }

    public void settCol(Integer tCol) {
        this.tCol = tCol;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, String> dimensions) {
        this.dimensions = dimensions;
    }

}
