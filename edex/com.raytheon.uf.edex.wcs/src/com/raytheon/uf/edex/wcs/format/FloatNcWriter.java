/**
 * Copyright 09/24/12 Raytheon Company.
 *
 * Unlimited Rights
 * This software was developed pursuant to Contract Number 
 * DTFAWA-10-D-00028 with the US Government. The US Government’s rights 
 * in and to this copyrighted software are as specified in DFARS
 * 252.227-7014 which was made part of the above contract. 
 */
package com.raytheon.uf.edex.wcs.format;

import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.nc4.NcVariable;
import com.raytheon.uf.common.nc4.NcVariable.FloatVariable;
import com.raytheon.uf.common.nc4.NetcdfException;

/**
 * Writes float record data to netcdf
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 12, 2013            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class FloatNcWriter extends NcWriter<FloatVariable> {

    @Override
    public Class<FloatVariable> getVarClass() {
        return FloatVariable.class;
    }

    @Override
    public void write(NcVariable var, int[] start, IDataRecord irecord)
            throws NetcdfException {
        FloatDataRecord record = (FloatDataRecord) irecord;
        FloatVariable variable = (FloatVariable) var;
        int[] shape = getShape(record);
        variable.putVar(start, shape, record.getFloatData());
    }

}
