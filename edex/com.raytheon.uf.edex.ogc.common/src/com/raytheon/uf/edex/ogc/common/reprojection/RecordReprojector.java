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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IntegerDataRecord;
import com.raytheon.uf.common.datastorage.records.LongDataRecord;
import com.raytheon.uf.common.datastorage.records.ShortDataRecord;
import com.raytheon.uf.common.geospatial.IGridGeometryProvider;
import com.raytheon.uf.common.geospatial.ISpatialEnabled;
import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.geospatial.SpatialException;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * This class provides functions for reprojecting the data contained within a
 * record. These ogc specific functions were removed from {@link PluginDao} to
 * here.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2013 1638       mschenke    Code moved from PluginDao to clean up dependencies
 * 
 * </pre>
 * 
 */
public class RecordReprojector {

    private PluginDataObject record;

    private IDataStore dataStore;

    /**
     * Default constructor.
     * 
     * @param record
     *            the record on which to perform operations.
     */
    public RecordReprojector(PluginDataObject record, IDataStore dataStore) {
        this.record = record;
        this.dataStore = dataStore;
    }

    /**
     * @param record
     * @param crs
     *            target crs for projected data
     * @param envelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws ReprojectionException
     * @throws Exception
     */
    public ReferencedDataRecord getProjected(CoordinateReferenceSystem crs,
            Envelope envelope) throws ReprojectionException {
        ReferencedEnvelope targetEnv = new ReferencedEnvelope(
                envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(),
                envelope.getMaxY(), crs);
        return getProjected(targetEnv);
    }

    /**
     * Get value stored in datastore for spatial coordinate.
     * 
     * @param crs
     * @param coord
     * @param defaultReturn
     *            returned if no data found at coordinate
     * @return
     * @throws InterrogationException
     */
    public double getDatastoreValue(CoordinateReferenceSystem crs, Coordinate coord,
            double defaultReturn) throws InterrogationException {
        GridGeometry2D geom;
        try {
            geom = getGridGeometry2D();
        } catch (SpatialException e) {
            throw new InterrogationException(
                    "Problem getting grid geometry for record: " + record, e);
        }
        // TODO a cache would probably be good here
        double rval = defaultReturn;
        try {
            DataReprojector reprojector = getDataReprojector();
            IDataRecord data = reprojector.getProjectedPoints(
                    record.getDataURI(), geom, crs, new Coordinate[] { coord });
            Double res = extractSingle(data);
            if (res != null) {
                rval = res;
            }
        } catch (ReprojectionException | UnknownDataRecordType e) {
            throw new InterrogationException(
                    "Problem reprojecting data for record" + record, e);
        }
        return rval;
    }

    /**
     * @param record
     * @param crs
     *            target crs for projected data
     * @param envelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws ReprojectionException
     * @throws Exception
     */
    public GridCoverage2D getProjectedCoverage(CoordinateReferenceSystem crs,
            Envelope envelope) throws ReprojectionException {
        ReferencedEnvelope targetEnv = new ReferencedEnvelope(
                envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(),
                envelope.getMaxY(), crs);
        return getProjectedCoverage(targetEnv);
    }

    /**
     * @param targetEnvelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws ReprojectionException
     * @throws Exception
     */
    public ReferencedDataRecord getProjected(ReferencedEnvelope targetEnvelope)
            throws ReprojectionException {
        GridGeometry2D geom;
        try {
            geom = getGridGeometry2D();
        } catch (SpatialException e) {
            throw new ReprojectionException(
                    "Problem getting grid geometry for record " + record, e);
        }
        ReferencedDataRecord rval;
        if (geom != null) {
            DataReprojector reprojector = getDataReprojector();
            rval = reprojector.getReprojected(record.getDataURI(), geom,
                    targetEnvelope);
        } else {
            throw new ReprojectionException(record.getClass()
                    + "is neither spatially enabled nor a geometry provider");
        }
        return rval;
    }

    /**
     * @param targetEnvelope
     *            bounding box in target crs
     * @return null if envelope is disjoint with data bounds
     * @throws ReprojectionException
     */
    public GridCoverage2D getProjectedCoverage(ReferencedEnvelope envelope)
            throws ReprojectionException {
        GridGeometry2D geom;
        try {
            geom = getGridGeometry2D();
        } catch (SpatialException e) {
            throw new ReprojectionException(
                    "Problem getting grid geometry for record " + record, e);
        }
        GridCoverage2D rval;
        if (geom != null) {
            DataReprojector reprojector = getDataReprojector();
            rval = reprojector.getReprojectedCoverage(record.getDataURI(),
                    geom, envelope);
        } else {
            throw new ReprojectionException(record.getClass()
                    + "is neither spatially enabled nor a geometry provider");
        }
        return rval;
    }

    public DataReprojector getDataReprojector() {
        return new DataReprojector(dataStore);
    }

    public ReferencedEnvelope getNativeEnvelope() throws FactoryException,
            SpatialException {
        GridGeometry2D geom = getGridGeometry2D();
        return new ReferencedEnvelope(geom.getEnvelope2D());
    }

    /**
     * @param record
     * @return first entry of data array for record
     * @throws UnknownDataRecordType
     */
    public static Double extractSingle(IDataRecord record)
            throws UnknownDataRecordType {
        Double rval = null;
        if (record == null) {
            return rval;
        }
        if (record instanceof ByteDataRecord) {
            byte[] data = ((ByteDataRecord) record).getByteData();
            rval = (double) data[0];
        } else if (record instanceof FloatDataRecord) {
            float[] data = ((FloatDataRecord) record).getFloatData();
            rval = (double) data[0];
        } else if (record instanceof IntegerDataRecord) {
            int[] data = ((IntegerDataRecord) record).getIntData();
            rval = (double) data[0];
        } else if (record instanceof ShortDataRecord) {
            short[] data = ((ShortDataRecord) record).getShortData();
            rval = (double) data[0];
        } else if (record instanceof LongDataRecord) {
            long[] data = ((LongDataRecord) record).getLongData();
            rval = (double) data[0];
        } else {
            throw new UnknownDataRecordType(
                    "Cannot extract from data record type: "
                            + record.getClass().getName());
        }
        return rval;
    }

    /**
     * @return grid geometry for object
     * @throws SpatialException
     *             if object doesn't contain spatial information
     */
    public GridGeometry2D getGridGeometry2D() throws SpatialException {
        GridGeometry2D rval;
        if (record instanceof ISpatialEnabled) {
            ISpatialObject spat = ((ISpatialEnabled) record).getSpatialObject();
            rval = MapUtil.getGridGeometry(spat);
        } else if (record instanceof IGridGeometryProvider) {
            rval = ((IGridGeometryProvider) record).getGridGeometry();
        } else {
            throw new SpatialException(record.getClass()
                    + " is not spatially enabled");
        }
        return rval;
    }
}
