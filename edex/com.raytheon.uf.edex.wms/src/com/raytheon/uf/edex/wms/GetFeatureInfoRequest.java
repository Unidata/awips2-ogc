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
package com.raytheon.uf.edex.wms;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.edex.ogc.common.OgcResponse;

/**
 * Represents an OGC Web Map Service get feature info request
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 28, 2012            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class GetFeatureInfoRequest extends GetMapRequest {

    protected String[] reqLayers;

    protected int featureCount = 1;

    protected Integer i;

    protected Integer j;

    protected MimeType infoFormat;

    public GetFeatureInfoRequest() {
    }

    public GetFeatureInfoRequest(GetMapRequest mapRequest, String[] reqLayers,
            Integer i, Integer j, MimeType infoFormat) {
        super(mapRequest);
        this.reqLayers = reqLayers;
        this.i = i;
        this.j = j;
        this.infoFormat = infoFormat;
    }

    @Override
    public OgcResponse execute(IWmsProvider provider) {
        return provider.getFeatureInfo(this);
    }

    public GetMapRequest getMapRequest() {
        return this;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
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

    /**
     * @return the reqLayers
     */
    public String[] getReqLayers() {
        return reqLayers;
    }

    /**
     * @param reqLayers
     *            the reqLayers to set
     */
    public void setReqLayers(String[] reqLayers) {
        this.reqLayers = reqLayers;
    }

    public MimeType getInfoFormat() {
        return infoFormat;
    }

    public void setInfoFormat(MimeType infoFormat) {
        this.infoFormat = infoFormat;
    }

}
