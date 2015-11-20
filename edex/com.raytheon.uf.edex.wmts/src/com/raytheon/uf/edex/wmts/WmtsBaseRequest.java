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

import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;
import com.raytheon.uf.edex.wms.BaseRequest;
import com.raytheon.uf.edex.wmts.WmtsProvider.WmtsOpType;

/**
 * Parent request object for Web Map Tile Service
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
public class WmtsBaseRequest extends BaseRequest<WmtsOpType> {

    public void execute(WmtsProvider provider, IOgcHttpResponse response) {
        provider.getCapabilities(this, response);
    }

}
