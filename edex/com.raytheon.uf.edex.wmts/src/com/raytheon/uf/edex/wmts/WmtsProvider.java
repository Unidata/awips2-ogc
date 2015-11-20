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

import java.io.InputStream;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;

/**
 * Interface for Web Map Tile Service providers. Concrete classes will be
 * version specific implementations.
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
public interface WmtsProvider {

    public static final String wmtsName = "wmts";

    public enum WmtsOpType {
        GetCapabilities, GetTile, GetFeatureInfo, GetLegendGraphic
    }

    public void getCapabilities(WmtsBaseRequest req, IOgcHttpResponse response);

    public void getTile(GetTileRequest req, IOgcHttpResponse response);

    public OgcResponse getError(WmtsException e, MimeType exceptionFormat);

    public void handlePost(InputStream in, IOgcHttpResponse response);

    public void getFeatureInfo(WmtsGetFeatureInfoRequest req,
            IOgcHttpResponse response);

}
