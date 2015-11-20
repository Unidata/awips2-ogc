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

import com.raytheon.uf.edex.ogc.common.OgcException;
import com.raytheon.uf.edex.wms.WmsException;

/**
 * Exception thrown during Web Map Tile Service operations. Includes error code
 * information.
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
public class WmtsException extends Exception {

    private static final long serialVersionUID = 3643250462683412896L;

    public enum Code {
        InvalidFormat, InvalidCRS, LayerNotDefined, StyleNotDefined, LayerNotQueryable, InvalidPoint, CurrentUpdateSequence, InvalidUpdateSequence, MissingDimensionValue, InvalidDimensionValue, OperationNotSupported, MissingParameterValue, InvalidParameterValue, InternalServerError, TileOutOfRange
    }

    protected Code code;

    public WmtsException(OgcException e) {
        super(e.getMessage());
        if (e.getCode().equals(OgcException.Code.InvalidFormat)) {
            this.code = Code.InvalidParameterValue;
        } else {
            this.code = Code.valueOf(e.getCode().toString());
        }
    }

    public WmtsException(WmsException e) {
        super(e.getMessage());
        this.code = Code.valueOf(e.getCode().toString());
    }

    public WmtsException(Code code) {
        super();
        this.code = code;
    }

    public WmtsException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public WmtsException(Code code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public WmtsException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

}
