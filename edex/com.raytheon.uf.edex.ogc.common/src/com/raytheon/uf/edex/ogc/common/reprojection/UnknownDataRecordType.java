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
package com.raytheon.uf.edex.ogc.common.reprojection;

/**
 * Exception thrown when the primitive data type is unsupported for an
 * IDataRecord
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 2, 2014            ekladstrup     Initial creation
 * 
 * </pre>
 * 
 * @author ekladstrup
 * @version 1.0
 */
public class UnknownDataRecordType extends Exception {

    private static final long serialVersionUID = -526046745205958918L;

    public UnknownDataRecordType() {
    }

    public UnknownDataRecordType(String message) {
        super(message);
    }

    public UnknownDataRecordType(Throwable cause) {
        super(cause);
    }

    public UnknownDataRecordType(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownDataRecordType(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
