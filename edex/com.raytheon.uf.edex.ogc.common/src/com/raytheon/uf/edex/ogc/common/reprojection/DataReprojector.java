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

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.media.jai.Interpolation;

import org.apache.commons.lang.ArrayUtils;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IntegerDataRecord;
import com.raytheon.uf.common.datastorage.records.ShortDataRecord;
import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.concurrent.KeyLock;
import com.raytheon.uf.common.util.concurrent.KeyLocker;
import com.raytheon.uf.edex.ogc.common.reprojection.AbstractDataReprojector.RequestWrapper;

/**
 * Retrieves data from the datastore in requested projection.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * Nov 2010               bclement  Initial creation
 * Nov 13, 2015  5087     bclement  Changed public methods to take GridGeometry
 *                                  instead of spatial object cache using soft
 *                                  references instead of in the datastore
 * Aug 30, 2016  5867     randerso  Updated for GeoTools 15.1
 * Mar 06, 2017  6165     nabowle   Update cache for Camel 2.18.2
 * Mar  2, 2021  8326     tgurney   Fix imports for Camel 3 + code cleanup
 *
 * </pre>
 *
 * @author bclement
 */
public class DataReprojector {

    private static final int CACHE_SIZE = Integer
            .getInteger("ogc.reprojector.cache.size", 512);

    protected IDataStore dataStore;

    protected String dataSetBase = "Data-";

    protected String dataSet = "Data-0";

    private AbstractDataReprojector<? extends IDataRecord> _typeProjector;

    protected static final IUFStatusHandler log = UFStatus
            .getHandler(DataReprojector.class);

    protected static final KeyLocker<String> locker = new KeyLocker<>();

    private static final Cache<String, IDataRecord> REFERENCE_CACHE = Caffeine
            .newBuilder().maximumSize(CACHE_SIZE).softValues().build();

    public DataReprojector(IDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param nativeGeom
     *            native bounds of dataset
     * @param crs
     *            desired crs of returned data
     * @param coords
     *            coordinates of requested data in requested crs
     * @return null if any of the coordinates are out of the bounds of the grid
     *         geometry
     * @throws ReprojectionException
     */
    public IDataRecord getProjectedPoints(String group,
            GridGeometry2D nativeGeom, CoordinateReferenceSystem crs,
            Coordinate[] coords) throws ReprojectionException {
        try {
            GridEnvelope2D nativeRange = nativeGeom.getGridRange2D();
            ReferencedEnvelope nativeEnv = new ReferencedEnvelope(
                    nativeGeom.getEnvelope2D());
            int nx = nativeRange.width;
            int ny = nativeRange.height;
            // get envelope in requested projection
            ReferencedEnvelope targetEnv = nativeEnv.transform(crs, true);
            // get target grid geometry
            GridGeometry2D targetGeom = getGridGeometry(targetEnv, nx, ny);
            Point[] points = new Point[coords.length];
            for (int i = 0; i < points.length; ++i) {
                Coordinate coord = coords[i];
                GridCoordinates2D point = getGridPoint(targetGeom, coord);
                // coordinate was out of bounds, bail
                if ((point.x < 0) || (point.x > nx) || (point.y < 0)
                        || (point.y > ny)) {
                    return null;
                }
                // need to repackage point due to pypies not knowing about
                // gridcoordinates2d
                points[i] = new Point(point);
            }
            String reprojectedDataset = buildDatasetName(crs);
            Request req = Request.buildPointRequest(points);
            return getDataRecordWithReproject(group, reprojectedDataset,
                    nativeGeom, crs, req);
        } catch (FactoryException | TransformException | PluginException e) {
            throw new ReprojectionException(
                    "Problem reprojecting data points from group " + group, e);
        }
    }

    /**
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param reprojectedDataset
     *            dataset name for reprojected data
     * @param geom
     *            bounds of dataset
     * @param crs
     *            desired crs of returned data
     * @param req
     *            datastore request object
     * @return
     * @throws Exception
     */
    protected IDataRecord getDataRecordWithReproject(String group,
            String reprojectedDataset, GridGeometry2D geom,
            CoordinateReferenceSystem crs, Request req)
            throws ReprojectionException {
        String cacheKey = group + reprojectedDataset;
        IDataRecord dataRecord = null;
        try {
            if (CRS.equalsIgnoreMetadata(crs,
                    geom.getCoordinateReferenceSystem())) {
                dataRecord = getDataRecord(group, dataSet, req);
            } else {
                IDataRecord fullRecord = REFERENCE_CACHE.getIfPresent(cacheKey);
                if (fullRecord == null) {
                    fullRecord = reprojectLocked(group, cacheKey, geom, crs);
                }
                dataRecord = getDataPerReq(fullRecord, req);
            }
        } catch (StorageException | FileNotFoundException e) {
            throw new ReprojectionException(
                    "Problem reprojecting data for group " + group, e);
        }
        return dataRecord;
    }

    /**
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param cacheKey
     *            cache key for reprojected data record
     * @param geom
     *            spatial bounds of requested dataset
     * @param crs
     *            desired crs of returned data
     * @return
     * @throws Exception
     */
    protected IDataRecord reprojectLocked(String group, String cacheKey,
            GridGeometry2D geom, CoordinateReferenceSystem crs)
            throws ReprojectionException {
        KeyLock<String> lock = null;
        IDataRecord dataRecord;
        try {
            // get reproject lock
            lock = locker.getLock(cacheKey);
            lock.lock();
            dataRecord = REFERENCE_CACHE.getIfPresent(cacheKey);

            // recheck that dataset still doesn't exist
            if (dataRecord == null) {
                // still not there, reproject
                dataRecord = reproject(geom, group, crs);
                REFERENCE_CACHE.put(cacheKey, dataRecord);
            }
            return dataRecord;
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    /**
     * @param group
     * @param dataset
     * @return true if dataset exists in datastore
     * @throws FileNotFoundException
     * @throws StorageException
     */
    protected boolean datasetExists(String group, String dataset)
            throws FileNotFoundException, StorageException {
        String[] datasets = dataStore.getDatasets(group);
        return ArrayUtils.contains(datasets, dataset);
    }

    /**
     * @param geom
     *            Grid geometry
     * @param coord
     *            desired geographic coordinate
     * @return grid point for coordinate
     * @throws PluginException
     */
    public static GridCoordinates2D getGridPoint(GridGeometry2D geom,
            Coordinate coord) throws PluginException {
        DirectPosition src = new DirectPosition2D(coord.x, coord.y);
        DirectPosition inGrid = new DirectPosition2D();
        try {
            MathTransform2D crsToGrid2D = geom
                    .getCRSToGrid2D(PixelOrientation.UPPER_LEFT);
            crsToGrid2D.transform(src, inGrid);
        } catch (Exception e) {
            throw new PluginException("Unable to get grid point for geometry",
                    e);
        }
        // floor of grid points should be upper left of pixel
        int x = (int) Math.floor(inGrid.getOrdinate(0));
        int y = (int) Math.floor(inGrid.getOrdinate(1));
        GridCoordinates2D rval = new GridCoordinates2D(x, y);
        return rval;
    }

    /**
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param geom
     *            spatial bounds of requested dataset
     * @param targetEnv
     *            bounds of requested data
     * @return null if target envelope is out of bounds for dataset
     * @throws Exception
     */
    public GridCoverage2D getReprojectedCoverage(String group,
            GridGeometry2D geom, ReferencedEnvelope targetEnv)
            throws ReprojectionException {
        ReferencedDataRecord rep = getReprojected(group, geom, targetEnv);
        if (rep == null) {
            return null;
        }
        ReferencedEnvelope re = rep.getEnvelope();
        IDataRecord record = rep.getRecord();
        try {
            return getTypeProjector(record).getGridCoverage(rep.getRecord(),
                    re);
        } catch (UnknownDataRecordType e) {
            throw new ReprojectionException(
                    "Problem getting grid coverage for group " + group, e);
        }
    }

    /**
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param geom
     *            spatial bounds of requested dataset
     * @param targetEnvelope
     *            bounds of requested data
     * @return null if target envelope is out of bounds for dataset
     * @throws Exception
     */
    public ReferencedDataRecord getReprojected(String group,
            GridGeometry2D geom, ReferencedEnvelope targetEnvelope)
            throws ReprojectionException {
        RequestWrapper req;
        try {
            req = getRequest(geom, targetEnvelope);
        } catch (Exception e) {
            throw new ReprojectionException(
                    "Problem calculating data request for group " + group, e);
        }
        if (req == null) {
            return null;
        }
        CoordinateReferenceSystem targetCrs = targetEnvelope
                .getCoordinateReferenceSystem();
        String reprojectedDataset = buildDatasetName(targetCrs);
        IDataRecord dataRecord = getDataRecordWithReproject(group,
                reprojectedDataset, geom, targetCrs, req.req);
        return new ReferencedDataRecord(dataRecord, req.env);
    }

    /**
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param targetDataset
     *            dataset name for requested data
     * @param req
     *            datastore request object
     * @return
     * @throws StorageException
     * @throws FileNotFoundException
     */
    protected IDataRecord getDataRecord(String group, String targetDataset,
            Request req) throws FileNotFoundException, StorageException {
        IDataRecord rval = dataStore.retrieve(group, targetDataset, req);
        return rval;
    }

    /**
     * Get a projector that is compatible with record
     *
     * @param record
     * @return
     * @throws UnknownDataRecordType
     */
    protected AbstractDataReprojector<? extends IDataRecord> getTypeProjector(
            IDataRecord record) throws UnknownDataRecordType {
        if ((_typeProjector == null) || !_typeProjector.compatible(record)) {
            if (record instanceof ByteDataRecord) {
                _typeProjector = new ByteDataReprojector();
            } else if (record instanceof FloatDataRecord) {
                _typeProjector = new FloatDataReprojector();
            } else if (record instanceof ShortDataRecord) {
                _typeProjector = new ShortDataReprojector();
            } else if (record instanceof IntegerDataRecord) {
                _typeProjector = new IntDataReprojector();
            } else {
                throw new UnknownDataRecordType(
                        "Unknown data store type for data record type: "
                                + record.getClass().getName());
            }
        }
        return _typeProjector;
    }

    /**
     * Gets the entire coverage from the store and reprojects it.
     *
     * @param spatial
     *            spatial object tied to requested dataset
     * @param group
     *            name of the datastore group that contains requested dataset
     * @param targetCRS
     *            desired crs of returned data
     * @return datarecord as per the request object
     * @throws Exception
     */
    protected IDataRecord reproject(GridGeometry2D geom, String group,
            CoordinateReferenceSystem targetCRS) throws ReprojectionException {
        try {
            IDataRecord original = getDataRecord(group, dataSet, Request.ALL);
            ReferencedEnvelope env = new ReferencedEnvelope(geom.getEnvelope());
            AbstractDataReprojector<? extends IDataRecord> typeProjector = getTypeProjector(
                    original);
            GridCoverage2D cov = typeProjector.getGridCoverage(original, env);
            GridCoverage2D reprojected = DataReprojectorMapUtil
                    .lenientReprojectCoverage(cov, targetCRS);
            IDataRecord reprojectedRecord;

            if (typeProjector instanceof FloatDataReprojector) {
                /*
                 * TODO So far, the problem that this fixes has only appeared
                 * with float data. If it happens with other data we can change
                 * this.
                 */
                GridCoverage2D maskCov = typeProjector.getMaskCoverage(original,
                        env);
                Interpolation interp = Interpolation
                        .getInstance(Interpolation.INTERP_NEAREST);
                GridCoverage2D reprojectedMask = (GridCoverage2D) DataReprojectorMapUtil.LENIENT_OPERATIONS
                        .resample(maskCov, targetCRS, null, interp);
                reprojectedRecord = typeProjector.extractData(reprojected,
                        reprojectedMask);
            } else {
                reprojectedRecord = typeProjector.extractData(reprojected);
            }

            reprojectedRecord.setGroup(group);
            reprojectedRecord.setName(buildDatasetName(targetCRS));
            return reprojectedRecord;
        } catch (UnknownDataRecordType | FileNotFoundException
                | StorageException e) {
            throw new ReprojectionException(
                    "Problem reprojecting data for group " + group, e);
        }
    }

    /**
     * Get the native grid geometry for the given spatial object.
     *
     * @param spatial
     *            the spatial object to get the grid geometry for.
     * @return the grid geometry or null if something went wrong.
     */
    protected GridGeometry2D getGridGeometry(ISpatialObject spatial) {
        return MapUtil.getGridGeometry(spatial);
    }

    /**
     * @param record
     *            data record containing full coverage
     * @param req
     *            datastore request object
     * @return result of applying request object to data record
     * @throws Exception
     */
    protected IDataRecord getDataPerReq(IDataRecord record, Request req)
            throws ReprojectionException {
        AbstractDataReprojector<? extends IDataRecord> typeProjector;
        try {
            typeProjector = getTypeProjector(record);
        } catch (UnknownDataRecordType e) {
            throw new ReprojectionException(
                    "Problem finding type projector for " + record, e);
        }
        IDataRecord rval;
        switch (req.getType()) {
        case ALL:
            rval = record;
            break;
        case POINT:
            rval = typeProjector.getDataPoints(record, req);
            break;
        case SLAB:
            rval = typeProjector.getDataSlice(record, req);
            break;
        case XLINE:
        case YLINE:
        default:
            throw new ReprojectionException(
                    "Data reprojector " + req.getType() + " not implemented");
        }
        return rval;
    }

    /**
     * @param env
     *            geographic bounds of data
     * @param nx
     *            length of x axis
     * @param ny
     *            length of y axis
     * @return
     */
    public static GridGeometry2D getGridGeometry(ReferencedEnvelope env, int nx,
            int ny) {
        // TODO cache
        GridGeometry2D mapGeom = null;
        mapGeom = new GridGeometry2D(new GeneralGridEnvelope(new int[] { 0, 0 },
                new int[] { nx, ny }, false), env);
        return mapGeom;
    }

    /**
     * Build up slice request for reprojected dataset
     *
     * @param geom
     * @param targetEnvelope
     *            bbox in crs
     * @return null if envelope is outside of data bounds
     * @throws TransformException
     * @throws MismatchedDimensionException
     * @throws FactoryException
     */
    public static RequestWrapper getRequest(GridGeometry2D geom,
            ReferencedEnvelope targetEnvelope)
            throws MismatchedDimensionException, TransformException,
            FactoryException {
        RequestWrapper rval = null;
        CoordinateReferenceSystem targetCrs = targetEnvelope
                .getCoordinateReferenceSystem();
        ReferencedEnvelope nativeEnv = new ReferencedEnvelope(
                geom.getEnvelope2D());
        // get full bounds of reprojected dataset
        ReferencedEnvelope dataEnv = nativeEnv.transform(targetCrs, true);
        if (!dataEnv.intersects((Envelope) targetEnvelope)) {
            // request and data envelopes are disjoint, return null
            return null;
        }
        // get grid geometry for reprojected dataset
        GridEnvelope2D geomextent = geom.getGridRange2D();
        int[] dims = { geomextent.width, geomextent.height };
        // we have to create a new GridGeometry2D so that we can be sure it uses
        // the CRS of the target envelope. dataEnv was transformed to that CRS
        // when it was created.
        GridGeometry2D dataInTargetCRS = getGridGeometry(dataEnv, dims[0],
                dims[1]);
        if (dataEnv.contains((Envelope) targetEnvelope)) {
            // requested slice is entirely inside data bounds
            // build slice based on requested bounds
            rval = getSubSlice(dataInTargetCRS, targetEnvelope, dims);
        } else {
            // build slice based on intersection
            Envelope intersection = targetEnvelope.intersection(dataEnv);
            rval = getSubSlice(dataInTargetCRS, intersection, dims);
        }
        return rval;
    }

    /**
     * @param geom
     *            grid geometry for projected dataset
     * @param env
     *            geographic bounds for slice
     * @param dims
     *            dimensions of dataset
     * @return grid slice that corresponds to env
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    protected static RequestWrapper getSubSlice(GridGeometry2D geom,
            Envelope env, int[] dims)
            throws MismatchedDimensionException, TransformException {
        RequestWrapper rval = new RequestWrapper();
        MathTransform2D crsToGrid2D = geom
                .getCRSToGrid2D(PixelOrientation.UPPER_LEFT);
        // find a slice that has data for entire envelope (can have extra)
        int[][] minmax = transformEnv(crsToGrid2D, env, dims);
        MathTransform2D gridToCrs = crsToGrid2D.inverse();
        // find an envelope that matches the slice (could be a bit larger than
        // previous envelope)
        rval.env = transformGrid(gridToCrs, minmax,
                geom.getCoordinateReferenceSystem());
        rval.req = Request.buildSlab(minmax[0], minmax[1]);
        return rval;
    }

    /**
     * @param gridToCrs
     * @param minmax
     *            2d array holding slice
     * @param crs
     * @return
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    protected static ReferencedEnvelope transformGrid(MathTransform2D gridToCrs,
            int[][] minmax, CoordinateReferenceSystem crs)
            throws MismatchedDimensionException, TransformException {
        int[] min = minmax[0];
        int[] max = minmax[1];
        DirectPosition lower = new DirectPosition2D(min[0], min[1]);
        DirectPosition upper = new DirectPosition2D(max[0], max[1]);
        DirectPosition lowerCrs = gridToCrs.transform(lower, null);
        DirectPosition upperCrs = gridToCrs.transform(upper, null);
        double x0 = lowerCrs.getOrdinate(0);
        double x1 = upperCrs.getOrdinate(0);
        // handle y axis flip
        double y0 = upperCrs.getOrdinate(1);
        double y1 = lowerCrs.getOrdinate(1);
        return new ReferencedEnvelope(x0, x1, y0, y1, crs);
    }

    /**
     * transforms crs coordinates to grid indexes using given math transform
     *
     * @param crsToGrid
     * @param env
     * @param dims
     *            max bounds to be limited to
     * @return an array with [[minx, miny], [maxx, maxy]]
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    protected static int[][] transformEnv(MathTransform2D crsToGrid,
            Envelope env, int[] dims)
            throws MismatchedDimensionException, TransformException {
        DirectPosition lower = new DirectPosition2D(env.getMinX(),
                env.getMinY());
        DirectPosition upper = new DirectPosition2D(env.getMaxX(),
                env.getMaxY());
        DirectPosition lowerGrid = crsToGrid.transform(lower, null);
        DirectPosition upperGrid = crsToGrid.transform(upper, null);
        int x0 = (int) Math.floor(lowerGrid.getOrdinate(0));
        // we want ceiling since slices are inclusive
        int x1 = (int) Math.ceil(upperGrid.getOrdinate(0));
        // handle y axis flip
        int y0 = (int) Math.floor(upperGrid.getOrdinate(1));
        // we want ceiling since slices are inclusive
        int y1 = (int) Math.ceil(lowerGrid.getOrdinate(1));
        // truncate requests to dataset dimensions
        if (x0 < 0) {
            x0 = 0;
        }
        if (y0 < 0) {
            y0 = 0;
        }
        if (x1 > dims[0]) {
            x1 = dims[0];
        }
        if (y1 > dims[1]) {
            y1 = dims[1];
        }
        return new int[][] { { x0, y0 }, { x1, y1 } };
    }

    /**
     * construct the dataset name based on the name of the crs.
     *
     * @param crs
     * @return
     */
    protected String buildDatasetName(CoordinateReferenceSystem crs) {
        Set<ReferenceIdentifier> ids = crs.getIdentifiers();
        String code;
        if ((ids == null) || ids.isEmpty()) {
            code = crs.getName().toString();
        } else {
            code = ids.stream().map(ReferenceIdentifier::toString)
                    .collect(Collectors.joining("-"));
        }
        return dataSetBase + code;
    }

    public IDataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(IDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public String getDataSetBase() {
        return dataSetBase;
    }

    public void setDataSetBase(String dataSetBase) {
        this.dataSetBase = dataSetBase;
    }

    public String getDataSet() {
        return dataSet;
    }

    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

}
