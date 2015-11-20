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
package com.raytheon.uf.edex.wmts.provider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBElement;

import net.opengis.ows.v_1_1_0.AllowedValues;
import net.opengis.ows.v_1_1_0.BoundingBoxType;
import net.opengis.ows.v_1_1_0.CodeType;
import net.opengis.ows.v_1_1_0.DCP;
import net.opengis.ows.v_1_1_0.DatasetDescriptionSummaryBaseType;
import net.opengis.ows.v_1_1_0.DomainMetadataType;
import net.opengis.ows.v_1_1_0.DomainType;
import net.opengis.ows.v_1_1_0.HTTP;
import net.opengis.ows.v_1_1_0.KeywordsType;
import net.opengis.ows.v_1_1_0.LanguageStringType;
import net.opengis.ows.v_1_1_0.Operation;
import net.opengis.ows.v_1_1_0.OperationsMetadata;
import net.opengis.ows.v_1_1_0.RequestMethodType;
import net.opengis.ows.v_1_1_0.ServiceIdentification;
import net.opengis.ows.v_1_1_0.ServiceProvider;
import net.opengis.ows.v_1_1_0.ValueType;
import net.opengis.ows.v_1_1_0.WGS84BoundingBoxType;
import net.opengis.wmts.v_1_0_0.Capabilities;
import net.opengis.wmts.v_1_0_0.ContentsType;
import net.opengis.wmts.v_1_0_0.Dimension;
import net.opengis.wmts.v_1_0_0.LayerType;
import net.opengis.wmts.v_1_0_0.LegendURL;
import net.opengis.wmts.v_1_0_0.Style;
import net.opengis.wmts.v_1_0_0.Themes;
import net.opengis.wmts.v_1_0_0.TileMatrixLimits;
import net.opengis.wmts.v_1_0_0.TileMatrixSet;
import net.opengis.wmts.v_1_0_0.TileMatrixSetLimits;
import net.opengis.wmts.v_1_0_0.TileMatrixSetLink;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ogc.common.OgcBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcDimension;
import com.raytheon.uf.edex.ogc.common.OgcGeoBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcOperationInfo;
import com.raytheon.uf.edex.ogc.common.OgcServiceInfo;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.jaxb.OgcJaxbManager;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;
import com.raytheon.uf.edex.ogc.common.spatial.CrsLookup;
import com.raytheon.uf.edex.wmts.WmtsBaseRequest;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.WmtsHttpHandler;
import com.raytheon.uf.edex.wmts.WmtsProvider.WmtsOpType;
import com.raytheon.uf.edex.wmts.reg.WmtsLayer;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.tiling.TileMatrix;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixFactory;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixRegistry;

/**
 * Builds Web Map Tile Service version 1.0.0 capabilities documents
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
public class CapabilitiesBuilder {

    protected String version = "1.0.0";

    protected OgcJaxbManager jaxbManager;

    protected WmtsSourceManager sourceManager;

    protected IUFStatusHandler log = UFStatus.getHandler(this.getClass());

    protected net.opengis.ows.v_1_1_0.ObjectFactory owsFactory = new net.opengis.ows.v_1_1_0.ObjectFactory();

    protected String defaultStyleName = "default";

    protected List<String> imageFormats;

    protected List<String> infoFormats;

    protected TileMatrixRegistry registry;

    protected static final double epsilon = 0.01;

    /**
     * @param jaxbManager
     * @param sourceAccessor
     * @param imageFormats
     * @param infoFormats
     */
    public CapabilitiesBuilder(OgcJaxbManager jaxbManager,
            List<String> imageFormats, List<String> infoFormats,
            TileMatrixRegistry registry, WmtsSourceManager sourceManager) {
        this.jaxbManager = jaxbManager;
        this.imageFormats = imageFormats;
        this.infoFormats = infoFormats;
        this.registry = registry;
        this.sourceManager = sourceManager;
    }

    /**
     * Build and send capabilities through servlet response
     * 
     * @param req
     * @param response
     * @throws WmtsException
     */
    public void build(WmtsBaseRequest req, IOgcHttpResponse response)
            throws WmtsException {
        // TODO support other formats for capabilities
        // String format = req.getFormat();

        Capabilities caps = new Capabilities();
        // TODO support only returning sections in request
        caps.setServiceIdentification(getServiceId());
        caps.setServiceProvider(getServiceProvider());
        caps.setOperationsMetadata(getOpMetadata(req));
        caps.setContents(getContents(req));
        caps.setThemes(getThemes());
        caps.setVersion(version);
        output(caps, response);
    }

    /**
     * Marshal capabilities through response
     * 
     * @param caps
     * @param response
     * @throws WmtsException
     */
    protected void output(Capabilities caps, IOgcHttpResponse response)
            throws WmtsException {
        try {
            response.setContentType("text/xml");
            jaxbManager.marshalToStream(caps, response.getOutputStream());
        } catch (Exception e) {
            // FIXME this error will never get to client since outputstream has
            // already been retrieved from response
            log.error("Problem marshalling jaxb class", e);
            throw new WmtsException(Code.InternalServerError);
        }
    }

    /**
     * Builds up service id object. Results are safe to cache.
     * 
     * @return
     */
    protected ServiceIdentification getServiceId() {
        ServiceIdentification rval = new ServiceIdentification();
        LanguageStringType title = new LanguageStringType();
        title.setValue("Web Map Tile Service");
        rval.setTitle(Arrays.asList(title));
        CodeType serviceType = new CodeType();
        serviceType.setValue("OGC WMTS");
        rval.setServiceType(serviceType);
        rval.setServiceTypeVersion(Arrays.asList(version));
        return rval;
    }

    /**
     * Builds up service provider object. Results safe to cache.
     * 
     * @return
     */
    protected ServiceProvider getServiceProvider() {
        ServiceProvider rval = new ServiceProvider();
        rval.setProviderName("Edex WMTS");
        return rval;
    }

    /**
     * Builds up operations metadata object. Results safe to cache.
     * 
     * @param req
     * @return
     */
    protected OperationsMetadata getOpMetadata(WmtsBaseRequest req) {
        OperationsMetadata rval = new OperationsMetadata();
        OgcServiceInfo<WmtsOpType> serviceinfo = req.getServiceinfo();
        rval.setOperation(getOperations(serviceinfo));
        return rval;
    }

    /**
     * @param serviceinfo
     * @return
     */
    protected List<Operation> getOperations(
            OgcServiceInfo<WmtsOpType> serviceinfo) {
        List<OgcOperationInfo<WmtsOpType>> from = serviceinfo.getOperations();
        List<Operation> rval = new ArrayList<Operation>(from.size());
        for (OgcOperationInfo<WmtsOpType> op : from) {
            rval.add(convert(op));
        }
        return rval;
    }

    /**
     * Create operation object from ogc operation info object.
     * 
     * @param op
     * @return
     */
    protected Operation convert(OgcOperationInfo<WmtsOpType> op) {
        Operation rval = new Operation();
        rval.setName(op.getType().toString());
        rval.setDCP(getDCPs(op));
        return rval;
    }

    /**
     * @param op
     * @return
     */
    protected List<DCP> getDCPs(OgcOperationInfo<WmtsOpType> op) {
        List<DCP> rval = new ArrayList<DCP>(2);
        String get = op.getHttpGetRes();
        if (get != null) {
            // assume KVP only for get
            rval.add(buildDCP(true, get, Arrays.asList("KVP")));
        }
        String post = op.getHttpPostRes();
        if (post != null) {
            rval.add(buildDCP(false, post, Arrays.asList("KVP")));
        }
        return rval;
    }

    /**
     * @param get
     *            true if the resulting DCP will be of type "Get"
     * @param href
     * @param encodingValues
     * @return
     */
    protected DCP buildDCP(boolean get, String href, List<String> encodingValues) {
        HTTP http = new HTTP();
        List<JAXBElement<RequestMethodType>> value = new ArrayList<JAXBElement<RequestMethodType>>(
                1);
        List<DomainType> constraints = new ArrayList<DomainType>(1);
        DomainType constraint = new DomainType();
        RequestMethodType req = new RequestMethodType();
        if (get) {
            req.setType("Get");
            constraint.setName("GetEncoding");
        } else {
            req.setTitle("Post");
            constraint.setName("PostEncoding");
        }
        req.setHref(href);
        AllowedValues allowedValues = new AllowedValues();
        allowedValues.setValueOrRange(getAsValues(encodingValues));
        constraint.setAllowedValues(allowedValues);
        req.setConstraint(constraints);
        JAXBElement<RequestMethodType> jxb;
        if (get) {
            jxb = owsFactory.createHTTPGet(req);
        } else {
            jxb = owsFactory.createHTTPPost(req);
        }
        value.add(jxb);
        http.setGetOrPost(value);
        DCP dcp = new DCP();
        dcp.setHTTP(http);
        return dcp;
    }

    /**
     * @param strs
     * @return list of ValueType objects with strs contents as values
     */
    protected List<Object> getAsValues(List<String> strs) {
        List<Object> rval = new ArrayList<Object>(strs.size());
        for (String str : strs) {
            ValueType value = new ValueType();
            value.setValue(str);
            rval.add(value);
        }
        return rval;
    }

    /**
     * @param req
     * @return
     * @throws WmtsException
     */
    protected ContentsType getContents(WmtsBaseRequest req)
            throws WmtsException {
        ContentsType rval = new ContentsType();
        rval.setDatasetDescriptionSummary(getDataDesc(req));
        rval.setTileMatrixSet(getTileMatrixSet());
        return rval;
    }

    /**
     * @param req
     * @return
     * @throws WmtsException
     */
    protected List<JAXBElement<DatasetDescriptionSummaryBaseType>> getDataDesc(
            WmtsBaseRequest req) throws WmtsException {
        List<WmtsLayer> layers = getLayers();
        List<JAXBElement<DatasetDescriptionSummaryBaseType>> rval = new ArrayList<JAXBElement<DatasetDescriptionSummaryBaseType>>(
                layers.size());
        for (WmtsLayer layer : layers) {
            rval.add(convert(req, layer));
        }
        return rval;
    }

    /**
     * @param req
     * @param layer
     * @throws WmtsException
     */
    protected JAXBElement<DatasetDescriptionSummaryBaseType> convert(
            WmtsBaseRequest req, WmtsLayer layer) throws WmtsException {
        LayerType rval = new LayerType();
        rval.setIdentifier(stringToCode(layer.getIdentifier()));
        rval.setTitle(stringToLangString(layer.getTitle()));
        rval.setKeywords(getKeywords(layer.getKeywords()));
        rval.setWGS84BoundingBox(getCrs84Bbox(layer.getGeoBoundingBox()));
        rval.setBoundingBox(getBbox(layer.getBboxes()));
        rval.setStyle(getStyles(req, layer));
        rval.setFormat(imageFormats);
        rval.setInfoFormat(infoFormats);
        rval.setDimension(getDimensions(layer.getDimensions()));
        rval.setTileMatrixSetLink(getTileMatrixSetLinks(layer));
        return owsFactory.createDatasetDescriptionSummary(rval);
    }

    /**
     * @param layer
     * @return
     * @throws WmtsException
     */
    protected List<TileMatrixSetLink> getTileMatrixSetLinks(WmtsLayer layer)
            throws WmtsException {
        OgcGeoBoundingBox bbox = layer.getGeoBoundingBox();
        if (bbox == null) {
            return new ArrayList<TileMatrixSetLink>(0);
        }
        List<TileMatrixSetLink> rval = new ArrayList<TileMatrixSetLink>(2);
        for (String tsetName : layer.getTileMatrixSets()) {
            com.raytheon.uf.edex.wmts.tiling.TileMatrixSet tset = registry
                    .getTileMatrixSet(tsetName);
            rval.add(createMatrixSetLink(bbox, tset));
        }
        return rval;
    }

    /**
     * @param bbox
     * @return
     * @throws WmtsException
     */
    protected TileMatrixSetLink createMatrixSetLink(OgcGeoBoundingBox bbox,
            com.raytheon.uf.edex.wmts.tiling.TileMatrixSet set)
            throws WmtsException {
        TileMatrixSetLink rval = new TileMatrixSetLink();
        rval.setTileMatrixSet(set.getIdentifier());
        ReferencedEnvelope crs84Bounds = new ReferencedEnvelope(bbox.getMinx(),
                bbox.getMaxx(), bbox.getMiny(), bbox.getMaxy(),
                MapUtil.LATLON_PROJECTION);
        try {
            CoordinateReferenceSystem targetCrs = CrsLookup.lookup(set
                    .getSupportedCrs());
            ReferencedEnvelope dataBounds = transformCrs84Bounds(crs84Bounds,
                    targetCrs, set.getBounds());
            ReferencedEnvelope tileBounds = set.getBounds();
            if (!dataBounds.equals(tileBounds)
                    && !dataBounds.covers(tileBounds)) {
                rval.setTileMatrixSetLimits(computeLimits(set, dataBounds,
                        tileBounds));
            }
        } catch (Exception e) {
            log.error("Problem finding layer bounds", e);
            throw new WmtsException(Code.InternalServerError);
        }
        return rval;
    }

    /**
     * Transforms bounds to target crs. Attempts to fix projection errors caused
     * by lat bounds being 90 or -90
     * 
     * @param bounds
     * @param targetCrs
     * @return
     * @throws TransformException
     * @throws FactoryException
     */
    protected ReferencedEnvelope transformCrs84Bounds(
            ReferencedEnvelope bounds, CoordinateReferenceSystem targetCrs,
            ReferencedEnvelope maxTargetBounds) throws TransformException,
            FactoryException {
        try {
            return bounds.transform(targetCrs, true);
        } catch (TransformException e) {
            // could be pole issues
            double maxy = bounds.getMaxY();
            double miny = bounds.getMinY();
            double minx = bounds.getMinX();
            double maxx = bounds.getMaxX();
            if (gte(maxy, 90) && lte(miny, 90) && gte(maxx, 180)
                    && lte(minx, -180)) {
                // total globe, return max target
                return maxTargetBounds;
            }
            // might cover all y axis, but not all x, try to trim y axis
            // FIXME 89.9 is too arbitrary, we need to find a way to get the
            // smallest trim we can for the bounds
            if (lte(miny, -90)) {
                miny = -89.9;
            }
            if (gte(maxy, 90)) {
                maxy = 89.9;
            }
            bounds.init(minx, maxx, miny, maxy);
            return bounds.transform(targetCrs, true);
        }
    }

    protected boolean gte(double a, double b) {
        if (a == b || a > b) {
            return true;
        }
        return eq(a, b);
    }

    protected boolean eq(double a, double b) {
        double diff = Math.abs(a - b);
        if (a == 0 || b == 0) {
            return diff < epsilon;
        } else {
            // relative
            double absA = Math.abs(a);
            double absB = Math.abs(b);
            return diff / (absA + absB) < epsilon;
        }
    }

    protected boolean lte(double a, double b) {
        if (a == b || a < b) {
            return true;
        }
        return eq(a, b);
    }

    /**
     * @param tileSet
     * @param dataBounds
     * @param tileBounds
     * @return
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    protected TileMatrixSetLimits computeLimits(
            com.raytheon.uf.edex.wmts.tiling.TileMatrixSet tileSet,
            ReferencedEnvelope dataBounds, ReferencedEnvelope tileBounds)
            throws MismatchedDimensionException, TransformException {
        TileMatrixSetLimits rval = new TileMatrixSetLimits();
        TileMatrix[] entries = tileSet.getMatrixEntries();
        List<TileMatrixLimits> limits = new ArrayList<TileMatrixLimits>(
                entries.length);
        for (TileMatrix entry : entries) {
            String matrixName = TileMatrixFactory.getMatrixId(
                    tileSet.getIdentifier(), entry);
            limits.add(computeLimits(matrixName, entry, dataBounds, tileBounds));
        }
        rval.setTileMatrixLimits(limits);
        return rval;
    }

    /**
     * @param matrixName
     * @param entry
     * @param dataBounds
     * @param tileBounds
     * @return
     * @throws MismatchedDimensionException
     * @throws TransformException
     */
    protected TileMatrixLimits computeLimits(String matrixName,
            TileMatrix entry, ReferencedEnvelope dataBounds,
            ReferencedEnvelope tileBounds) throws MismatchedDimensionException,
            TransformException {
        // TODO this should be cached
        GridGeometry2D gridGeom = new GridGeometry2D(new GridEnvelope2D(0, 0,
                entry.getMatrixWidth(), entry.getMatrixHeight()), tileBounds);
        MathTransform toGrid = gridGeom
                .getCRSToGrid2D(PixelOrientation.UPPER_LEFT);
        DirectPosition2D upperLeft = new DirectPosition2D();
        DirectPosition2D lowerRight = new DirectPosition2D();
        // remember the y axis flip between crs and grid
        DirectPosition2D crsUpperLeft = new DirectPosition2D(
                dataBounds.getMinX(), dataBounds.getMaxY());
        DirectPosition2D crslowerRight = new DirectPosition2D(
                dataBounds.getMaxX(), dataBounds.getMinY());
        toGrid.transform(crsUpperLeft, upperLeft);
        toGrid.transform(crslowerRight, lowerRight);
        TileMatrixLimits rval = new TileMatrixLimits();
        rval.setMinTileCol(getFloor(upperLeft.x));
        rval.setMinTileRow(getFloor(upperLeft.y));
        rval.setMaxTileCol(getFloor(lowerRight.x));
        rval.setMaxTileRow(getFloor(lowerRight.y));
        rval.setTileMatrix(matrixName);
        return rval;
    }

    protected BigInteger getFloor(double d) {
        long l = (long) Math.floor(d);
        return BigInteger.valueOf(l);
    }

    /**
     * @param dimensions
     * @return
     */
    protected List<Dimension> getDimensions(Collection<OgcDimension> dims) {
        if (dims == null) {
            return new ArrayList<Dimension>(0);
        }
        List<Dimension> rval = new ArrayList<Dimension>(dims.size());
        for (OgcDimension from : dims) {
            Dimension to = new Dimension();
            to.setIdentifier(stringToCode(from.getName()));
            String units = from.getUnits();
            if (units != null) {
                DomainMetadataType dmt = new DomainMetadataType();
                dmt.setValue(units);
                to.setUOM(dmt);
            }
            to.setUnitSymbol(from.getUnitSymbol());
            to.setValue(from.getValues());
            to.setDefault(from.getDefaultVal());
            // we could just use the default value
            if (from.getName().equalsIgnoreCase("time")) {
                to.setDefault("current");
            } else {
                to.setDefault(from.getDefaultVal());
            }
            rval.add(to);
        }
        return rval;
    }

    protected List<Style> getStyles(WmtsBaseRequest req, WmtsLayer layer) {
        Collection<OgcStyle> styles = layer.getStyles();
        if (styles == null || styles.isEmpty()) {
            return getDefaultStyles();
        }
        List<Style> rval = new ArrayList<Style>(styles.size());
        for (OgcStyle in : styles) {
            Style out = new Style();
            out.setIdentifier(stringToCode(in.getName()));
            out.setIsDefault(in.isDefault());
            String legendUrl = in.getLegendUrl();
            if (legendUrl != null) {
                out.setLegendURL(getLegendURL(req, legendUrl));
            }
            rval.add(out);
        }
        return rval;
    }

    protected List<LegendURL> getLegendURL(WmtsBaseRequest req, String url) {
        OgcServiceInfo<WmtsOpType> serviceinfo = req.getServiceinfo();
        OgcOperationInfo<WmtsOpType> legendOp = getLegendOp(serviceinfo);
        LegendURL rval = new LegendURL();
        rval.setHref(legendOp.getHttpGetRes() + "&request="
                + WmtsHttpHandler.LEG_PARAM + url);
        return Arrays.asList(rval);
    }

    private OgcOperationInfo<WmtsOpType> getLegendOp(
            OgcServiceInfo<WmtsOpType> ogcServiceInfo) {
        List<OgcOperationInfo<WmtsOpType>> ops = ogcServiceInfo.getOperations();
        for (OgcOperationInfo<WmtsOpType> op : ops) {
            if (op.getType().equals(WmtsOpType.GetLegendGraphic)) {
                return op;
            }
        }
        return null;
    }

    protected List<Style> getDefaultStyles() {
        Style rval = new Style();
        rval.setIdentifier(stringToCode(defaultStyleName));
        rval.setIsDefault(true);
        return Arrays.asList(rval);
    }

    /**
     * @param bboxs
     * @return
     */
    protected List<JAXBElement<? extends BoundingBoxType>> getBbox(
            Collection<OgcBoundingBox> bboxs) {
        if (bboxs == null) {
            return new ArrayList<JAXBElement<? extends BoundingBoxType>>(0);
        }
        List<JAXBElement<? extends BoundingBoxType>> rval = new ArrayList<JAXBElement<? extends BoundingBoxType>>(
                bboxs.size());
        for (OgcBoundingBox from : bboxs) {
            BoundingBoxType to = owsFactory.createBoundingBoxType();
            to.setCrs(from.getCrs());
            to.setDimensions(BigInteger.valueOf(2));
            to.setLowerCorner(Arrays.asList(from.getMinx(), from.getMiny()));
            to.setUpperCorner(Arrays.asList(from.getMaxx(), from.getMaxy()));
            rval.add(owsFactory.createBoundingBox(to));
        }
        return rval;
    }

    protected List<WGS84BoundingBoxType> getCrs84Bbox(OgcGeoBoundingBox bbox) {
        ArrayList<WGS84BoundingBoxType> rval = new ArrayList<WGS84BoundingBoxType>(
                1);
        if (bbox != null) {
            WGS84BoundingBoxType to = new WGS84BoundingBoxType();
            to.setLowerCorner(Arrays.asList(bbox.getMinx(), bbox.getMiny()));
            to.setUpperCorner(Arrays.asList(bbox.getMaxx(), bbox.getMaxy()));
            rval.add(to);
        }
        return rval;
    }

    protected List<KeywordsType> getKeywords(Collection<String> values) {
        if (values == null) {
            return new ArrayList<KeywordsType>(0);
        }
        List<KeywordsType> rval = new ArrayList<KeywordsType>(values.size());
        for (String value : values) {
            KeywordsType kw = new KeywordsType();
            kw.setKeyword(stringToLangString(value));
            rval.add(kw);
        }
        return rval;
    }

    protected List<LanguageStringType> stringToLangString(String str) {
        List<LanguageStringType> rval = new ArrayList<LanguageStringType>(1);
        if (str != null) {
            LanguageStringType lst = new LanguageStringType();
            lst.setValue(str);
            rval.add(lst);
        }
        return rval;
    }

    protected CodeType stringToCode(String str) {
        CodeType rval = new CodeType();
        if (str != null) {
            rval.setValue(str);
        }
        return rval;
    }

    protected List<WmtsLayer> getLayers() throws WmtsException {
        List<WmtsLayer> rval = new ArrayList<WmtsLayer>();
        for (WmtsSource source : sourceManager.getSources()) {
            rval.addAll(source.listLayers());
        }
        return rval;
    }

    /**
     * @return
     */
    protected List<TileMatrixSet> getTileMatrixSet() {
        // TODO these should be coming from a registry
        List<TileMatrixSet> rval = new ArrayList<TileMatrixSet>(2);
        rval.add(getTileMatrixSet(TileMatrixRegistry.crs84TSetName));
        rval.add(getTileMatrixSet(TileMatrixRegistry.googleTSetName));
        return rval;
    }

    /**
     * @param crs84TileSet2
     * @return
     */
    protected TileMatrixSet getTileMatrixSet(String tileSetName) {
        com.raytheon.uf.edex.wmts.tiling.TileMatrixSet tileSet = registry
                .getTileMatrixSet(tileSetName);
        TileMatrixSet rval = new TileMatrixSet();
        rval.setIdentifier(stringToCode(tileSet.getIdentifier()));
        rval.setSupportedCRS(tileSet.getSupportedCrs());
        rval.setTileMatrix(getTileMatrix(tileSet.getIdentifier(),
                tileSet.getMatrixEntries()));
        ReferencedEnvelope bounds = tileSet.getBounds();
        BoundingBoxType bbox = owsFactory.createBoundingBoxType();
        bbox.setCrs(tileSet.getSupportedCrs());
        bbox.setLowerCorner(Arrays.asList(bounds.getMinX(), bounds.getMinY()));
        bbox.setUpperCorner(Arrays.asList(bounds.getMaxX(), bounds.getMaxY()));
        rval.setBoundingBox(owsFactory.createBoundingBox(bbox));
        return rval;
    }

    /**
     * @param identifier
     * @param matrixEntries
     * @return
     */
    protected List<net.opengis.wmts.v_1_0_0.TileMatrix> getTileMatrix(
            String identifier, TileMatrix[] matrixEntries) {
        List<net.opengis.wmts.v_1_0_0.TileMatrix> rval = new ArrayList<net.opengis.wmts.v_1_0_0.TileMatrix>(
                matrixEntries.length);
        for (TileMatrix from : matrixEntries) {
            net.opengis.wmts.v_1_0_0.TileMatrix to = new net.opengis.wmts.v_1_0_0.TileMatrix();
            String id = TileMatrixFactory.getMatrixId(identifier, from);
            to.setIdentifier(stringToCode(id));
            to.setScaleDenominator(from.getScaleDenominator());
            ReferencedEnvelope bounds = from.getBounds();
            to.setTopLeftCorner(Arrays.asList(bounds.getMinX(),
                    bounds.getMaxY()));
            to.setTileWidth(BigInteger.valueOf(from.getTileWidth()));
            to.setTileHeight(BigInteger.valueOf(from.getTileHeight()));
            to.setMatrixWidth(BigInteger.valueOf(from.getMatrixWidth()));
            to.setMatrixHeight(BigInteger.valueOf(from.getMatrixHeight()));
            rval.add(to);
        }
        return rval;
    }

    /**
     * @return
     */
    protected List<Themes> getThemes() {
        // TODO Auto-generated method stub
        return new ArrayList<Themes>(0);
    }

}
