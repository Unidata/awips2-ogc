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

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;

/**
 * Request object to get feature (GML) information for a point on a tile
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
public class WmtsGetFeatureInfoRequest extends GetTileRequest {

    protected Integer i;

    protected Integer j;

    protected MimeType infoFormat;

    public WmtsGetFeatureInfoRequest(GetTileRequest getTile, Integer i,
            Integer j, MimeType infoFormat) {
        super(getTile.layer, getTile.style, getTile.tMatrixSet,
                getTile.tMatrix, getTile.tRow, getTile.tCol, getTile.time,
                getTile.elevation, getTile.dimensions);
        this.i = i;
        this.j = j;
        this.infoFormat = infoFormat;
    }

    @Override
    public void execute(WmtsProvider provider, IOgcHttpResponse response) {
        provider.getFeatureInfo(this, response);
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public Integer getJ() {
        return j;
    }

    public void setJ(Integer j) {
        this.j = j;
    }

    public MimeType getInfoFormat() {
        return infoFormat;
    }

    public void setInfoFormat(MimeType infoFormat) {
        this.infoFormat = infoFormat;
    }

}
