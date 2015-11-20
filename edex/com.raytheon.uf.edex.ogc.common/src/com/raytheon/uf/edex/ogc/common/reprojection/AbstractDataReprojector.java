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

import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;

import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.records.IDataRecord;

/**
 * Base class for reprojection utilities that wrap IDataRecord objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 28, 2012            bclement     Initial creation
 * 
 * </pre>
 * 
 * @param <T>
 */
public abstract class AbstractDataReprojector<T extends IDataRecord> {

    public static class RequestWrapper {
        public Request req;

        public ReferencedEnvelope env;
    }

    /**
     * Copy data record into geotools grid coverage object
     * 
     * @param dataRecord
     *            datset
     * @param env
     *            geographics bounds for dataset
     * @return
     */
    protected abstract GridCoverage2D getGridCoverage(IDataRecord dataRecord,
            ReferencedEnvelope env);

    /**
     * Copy data record into geotools grid coverage object
     * 
     * @param dataRecord
     *            datset
     * @param env
     *            geographics bounds for dataset
     * @return
     */
    protected abstract GridCoverage2D getMaskCoverage(IDataRecord dataRecord,
            ReferencedEnvelope env);

    /**
     * Extract data from geotools coverage object into data record object
     * 
     * @param coverage
     * @return
     */
    protected abstract T extractData(GridCoverage2D coverage);

    /**
     * Extract data from geotools coverage object into data record object with a
     * mask for covering the non-data area.
     * 
     * @param coverage
     * @param maskCoverage
     * @return
     */
    protected abstract T extractData(GridCoverage2D coverage,
            GridCoverage2D maskCoverage);

    /**
     * Apply slab request to data record, returning result in a new data record
     * object.
     * 
     * @param dataRecord
     * @param req
     * @return
     */
    protected abstract T getDataSlice(IDataRecord dataRecord, Request req);

    /**
     * @param dataRecord
     * @return true if this object can operate on native type of data record
     */
    protected abstract boolean compatible(IDataRecord dataRecord);

    /**
     * Apply point request to data record, returning result in a new data record
     * object.
     * 
     * @param record
     * @param req
     * @return
     */
    protected abstract IDataRecord getDataPoints(IDataRecord record, Request req);

    /**
     * Construct a new geotools grid coverage object using data buffer
     * 
     * @param name
     *            name of coverage
     * @param data
     *            raw data
     * @param width
     * @param height
     * @param env
     *            geographic bounds of coverage
     * @return
     */
    public static GridCoverage2D constructGridCoverage(String name,
            DataBuffer data, int width, int height, ReferencedEnvelope env) {
        WritableRaster raster = RasterFactory.createBandedRaster(data, width,
                height, width, new int[] { 0 }, new int[] { 0 }, null);
        return new GridCoverageFactory().create(name, raster, env);
    }

}
