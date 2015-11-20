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
package com.raytheon.uf.edex.wms.provider;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.wms.v_1_3_0.Capability;
import net.opengis.wms.v_1_3_0.DCPType;
import net.opengis.wms.v_1_3_0.Exception;
import net.opengis.wms.v_1_3_0.Get;
import net.opengis.wms.v_1_3_0.HTTP;
import net.opengis.wms.v_1_3_0.Layer;
import net.opengis.wms.v_1_3_0.OnlineResource;
import net.opengis.wms.v_1_3_0.OperationType;
import net.opengis.wms.v_1_3_0.Post;
import net.opengis.wms.v_1_3_0.Request;
import net.opengis.wms.v_1_3_0.Service;
import net.opengis.wms.v_1_3_0.ServiceExceptionReport;
import net.opengis.wms.v_1_3_0.ServiceExceptionType;
import net.opengis.wms.v_1_3_0.WMSCapabilities;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayer;
import org.geotools.styling.StyledLayerDescriptor;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;
import org.springframework.context.ApplicationContext;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.ogc.common.OgcNamespace;
import com.raytheon.uf.edex.ogc.common.OgcOperationInfo;
import com.raytheon.uf.edex.ogc.common.OgcPrefix;
import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.ogc.common.OgcResponse.ErrorType;
import com.raytheon.uf.edex.ogc.common.OgcResponse.TYPE;
import com.raytheon.uf.edex.ogc.common.OgcServiceInfo;
import com.raytheon.uf.edex.ogc.common.feature.SimpleFeatureFormatter;
import com.raytheon.uf.edex.ogc.common.jaxb.OgcJaxbManager;
import com.raytheon.uf.edex.ogc.common.spatial.CrsLookup;
import com.raytheon.uf.edex.wms.BaseRequest;
import com.raytheon.uf.edex.wms.GetFeatureInfoRequest;
import com.raytheon.uf.edex.wms.GetLegendGraphicRequest;
import com.raytheon.uf.edex.wms.GetMapRequest;
import com.raytheon.uf.edex.wms.IWmsProvider;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;
import com.raytheon.uf.edex.wms.WmsHttpHandler;
import com.raytheon.uf.edex.wms.reg.WmsImage;
import com.raytheon.uf.edex.wms.reg.WmsSource;
import com.raytheon.uf.edex.wms.util.StyleUtility;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * OGC Web Map Service implementation for version 1.3.0
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
public class OgcWmsProvider implements IWmsProvider {

    protected static final String svcTitle = "EDEX Map Server";

    protected WmsLayerManager layerManager;

    protected OgcJaxbManager jaxbManager;

    private IUFStatusHandler log = UFStatus.getHandler(this.getClass());

    protected String resizeHint = "quality";

    protected OgcGetMapTranslator getMapTranslator;

    protected QName getMapName = new QName("GetMap", new Namespace(
            OgcPrefix.SLD, OgcNamespace.SLD));

    public OgcWmsProvider(WmsLayerManager layerManager,
            OgcJaxbManager jaxbManager, OgcGetMapTranslator getMapTranslator) {
        this.layerManager = layerManager;
        this.jaxbManager = jaxbManager;
        this.getMapTranslator = getMapTranslator;
    }

    @Override
    public OgcResponse getCapabilities(BaseRequest<WmsOpType> req) {
        OgcResponse rval;
        try {
            WMSCapabilities capabilities = new WMSCapabilities();
            OgcServiceInfo<WmsOpType> serviceinfo = req.getServiceinfo();
            capabilities.setService(getServiceInfo(serviceinfo));
            capabilities.setCapability(getMainCapability(req));
            rval = marshalResponse(capabilities);
        } catch (WmsException e) {
            rval = getError(e, req.getExceptionFormat());
        }
        return rval;
    }

    /**
     * @return
     */
    protected Service getServiceInfo(OgcServiceInfo<WmsOpType> serviceInfo) {
        Service rval = new Service();
        rval.setName(WMS_NAME);
        rval.setTitle(svcTitle);
        rval.setOnlineResource(getOLR(serviceInfo.getOnlineResource()));
        return rval;
    }

    protected OnlineResource getOLR(String href) {
        OnlineResource rval = new OnlineResource();
        rval.setHref(href);
        return rval;
    }

    protected Capability getMainCapability(BaseRequest<WmsOpType> req)
            throws WmsException {
        Capability rval = new Capability();
        OgcServiceInfo<WmsOpType> serviceinfo = req.getServiceinfo();
        rval.setRequest(getValidRequests(serviceinfo.getOperations()));
        rval.setException(getExceptionInfo());
        rval.setLayer(getLayerInfo(req));
        return rval;
    }

    /**
     * @return
     * @throws WmsException
     */
    protected Layer getLayerInfo(BaseRequest<WmsOpType> req)
            throws WmsException {
        Layer rval = new Layer();
        rval.setTitle(svcTitle);
        rval.setLayer(getAuthorizedLayers(req));
        return rval;
    }

    protected List<Layer> getAuthorizedLayers(BaseRequest<WmsOpType> req)
            throws WmsException {

        return layerManager.getLayers(req.getServiceinfo());
    }

    /**
     * @return
     */
    protected Exception getExceptionInfo() {
        Exception rval = new Exception();
        rval.setFormat(Arrays.asList("XML"));
        return rval;
    }

    /**
     * @param operations
     * @return
     */
    protected Request getValidRequests(
            List<OgcOperationInfo<WmsOpType>> operations) {
        Request rval = new Request();
        for (OgcOperationInfo<WmsOpType> op : operations) {
            OperationType opType = new OperationType();
            populateOpType(opType, op);
            switch (op.getType()) {
            case GetCapabilities:
                rval.setGetCapabilities(opType);
                break;
            case GetFeatureInfo:
                rval.setGetFeatureInfo(opType);
                break;
            case GetMap:
                rval.setGetMap(opType);
                break;
            case GetLegendGraphic:
                break;
            }
        }
        return rval;
    }

    protected void populateOpType(OperationType opType,
            OgcOperationInfo<WmsOpType> info) {
        opType.setFormat(info.getFormats());
        DCPType dcpt = new DCPType();
        HTTP http = new HTTP();
        if (info.hasHttpGet()) {
            Get get = new Get();
            get.setOnlineResource(getOLR(info.getHttpGetRes()));
            http.setGet(get);
        }
        if (info.hasHttpPost()) {
            Post post = new Post();
            post.setOnlineResource(getOLR(info.getHttpPostRes()));
            http.setPost(post);
        }
        dcpt.setHTTP(http);
        opType.setDCPType(Arrays.asList(dcpt));
    }

    protected OgcResponse marshalResponse(Object jaxbobject)
            throws WmsException {
        OgcResponse rval;
        try {
            String xml = jaxbManager.marshalToXml(jaxbobject);
            rval = new OgcResponse(xml, OgcResponse.TEXT_XML_MIME, TYPE.TEXT);
        } catch (JAXBException e) {
            log.error("Unable to marshal WFS response", e);
            throw new WmsException(Code.InternalServerError);
        }
        return rval;
    }

    @Override
    public OgcResponse handlePost(InputStream in) {
        Element root;
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(in);
            root = doc.getRootElement();
        } catch (java.lang.Exception e) {
            log.error("Unable to read post data", e);
            return getError(new WmsException(Code.InternalServerError),
                    OgcResponse.TEXT_XML_MIME);
        }
        if (root.getQName().equals(getMapName)) {
            return getMapPost(root);
        } else {
            return getError(new WmsException(Code.InvalidParameterValue,
                    "Post method only supported for GetMap operation"),
                    OgcResponse.TEXT_XML_MIME);
        }
    }

    protected OgcResponse getMapPost(Element root) {
        GetMapRequest translated;
        try {
            translated = getMapTranslator.translate(root);
        } catch (Throwable e) {
            log.error("Unable to converted getmap request", e);
            return getError(new WmsException(Code.InternalServerError),
                    OgcResponse.TEXT_XML_MIME);
        }
        return getMap(translated);
    }

    @Override
    public OgcResponse getMap(GetMapRequest req) {
        OgcResponse rval = null;
        StyledLayerDescriptor sld = req.getSld();
        String[] layers = req.getLayers();
        String[] styles = req.getStyles();
        String[] times = req.getTimes();
        String elevation = req.getElevation();
        Map<String, String> dimensions = req.getDimensions();
        boolean sldOnly = sld != null && layers == null;
        if (styles[0].equals("null")) {
            styles[0] = "";
        }
        if ((rval = checkGetMapArgs(req)) != null) {
            // there was a problem, return the error
            return rval;
        }
        boolean clear = parseTransparent(req.getTransparent());
        List<WmsImage> images;
        try {
            Iterator<?> it = ImageIO.getImageWritersByMIMEType(req.getFormat()
                    .toString());
            if (!it.hasNext()) {
                throw new WmsException(Code.InvalidFormat,
                        "Format not supported: " + req.getFormat());
            }
            Color color = parseColor(req.getBgcolor());
            if (req.getWidth() <= 0 || req.getHeight() <= 0) {
                throw new WmsException(Code.InvalidParameterValue,
                        "Invalid resolution");
            }
            String username = req.getUserName();
            String[] roles = req.getRoles();
            GridGeometry2D geom = createGridGeometry(req);
            double scale = getScale(geom);
            GetMapProcessor proc = new GetMapProcessor(layerManager, geom,
                    elevation, dimensions, scale, username, roles);
            if (sld == null) {
                images = proc.getMap(layers, styles, times);
            } else if (sldOnly) {
                images = proc.getMapSld(sld, times);
            } else {
                images = proc.getMapStyleLib(layers, styles, times, sld);
            }
            BufferedImage map = mergeWmsImages(images, clear, color, geom);
            rval = new OgcResponse(map, req.getFormat(), TYPE.IMAGE);
        } catch (WmsException e) {
            rval = getError(e, req.getExceptionFormat());
        }
        return rval;
    }

    protected Map<String, Style> getStyleMap(StyledLayerDescriptor sld) {
        if (sld == null) {
            return null;
        }
        StyledLayer[] layers = sld.getStyledLayers();
        Map<String, Style> rval = new HashMap<String, Style>();
        for (StyledLayer l : layers) {
            if (l instanceof NamedLayer) {
                NamedLayer nl = (NamedLayer) l;
                Style[] styles = nl.getStyles();
                if (styles != null) {
                    for (Style s : styles) {
                        rval.put(s.getName(), s);
                    }
                }
            }
        }
        return rval;
    }

    protected Envelope parseEnvString(String str, CoordinateReferenceSystem crs)
            throws WmsException {
        Envelope rval = null;
        try {
            if (str != null) {
                String[] parts = str.split(",");
                if (parts.length >= 4) {
                    double minx = Double.parseDouble(parts[0]);
                    double miny = Double.parseDouble(parts[1]);
                    double maxx = Double.parseDouble(parts[2]);
                    double maxy = Double.parseDouble(parts[3]);
                    if (minx > maxx || miny > maxy) {
                        throw new java.lang.Exception();
                    }
                    if (CrsLookup.isEpsgGeoCrs(crs)) {
                        // EPSG GeoCRS uses lat/lon axis order. Switch to match
                        // framework order of lon/lat
                        rval = new Envelope(miny, maxy, minx, maxx);
                    } else {
                        rval = new Envelope(minx, maxx, miny, maxy);
                    }
                } else {
                    throw new java.lang.Exception();
                }
            }
        } catch (Throwable e) {
            throw new WmsException(Code.InvalidParameterValue, "Invalid bbox");
        }
        return rval;
    }

    /**
     * @param crs
     * @return
     * @throws WmsException
     */
    protected CoordinateReferenceSystem parseCrs(String crs)
            throws WmsException {
        try {
            return CrsLookup.lookup(crs);
        } catch (Throwable e) {
            throw new WmsException(Code.InvalidCRS);
        }
    }

    protected Color parseColor(String color) throws WmsException {
        if (color == null) {
            return Color.white;
        }
        try {
            return Color.decode(color);
        } catch (NumberFormatException e) {
            throw new WmsException(Code.InvalidParameterValue,
                    "Invalid bgcolor: " + color);
        }
    }

    protected BufferedImage resize(BufferedImage input, int width, int height) {
        BufferedImage rval = input;
        // int type = transparent ? BufferedImage.TYPE_INT_ARGB
        // : BufferedImage.TYPE_INT_RGB;
        int type = BufferedImage.TYPE_INT_ARGB;
        boolean quality = !resizeHint.equalsIgnoreCase("speed");
        Object hint = quality ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                : RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        int h = quality ? input.getHeight() : height;
        int w = quality ? input.getWidth() : width;

        do {
            if (quality && w > width) {
                w /= 2;
                if (w < width) {
                    w = width;
                }
            } else {
                w = width;
            }
            if (quality && h > height) {
                h /= 2;
                if (h < height) {
                    h = height;
                }
            } else {
                h = height;
            }
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g = tmp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g.drawImage(rval, 0, 0, w, h, null);
            g.dispose();
            rval = tmp;
        } while (w != width || h != height);
        return rval;
    }

    /**
     * @param width
     * @param height
     * @return
     */
    protected BufferedImage createBlank(Integer width, Integer height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public static BufferedImage mergeWmsImages(List<WmsImage> images,
            boolean clear, Color bgcolor, GridGeometry2D geom)
            throws WmsException {
        CoordinateReferenceSystem crs = geom.getCoordinateReferenceSystem();
        Envelope2D env = geom.getEnvelope2D();
        GridEnvelope2D range = geom.getGridRange2D();
        GeneralEnvelope bounds = new GeneralEnvelope(crs);
        bounds.setEnvelope(env.getMinX(), env.getMinY(), env.getMaxX(),
                env.getMaxY());
        MapContent map = new MapContent();
        ReferencedEnvelope envelope = new ReferencedEnvelope(env, crs);
        map.setViewport(new MapViewport(envelope));
        try {
            populateMap(map, images, bounds);
            Rectangle dims = new Rectangle(range.width, range.height);
            ReferencedEnvelope re = new ReferencedEnvelope(env, crs);
            if (clear) {
                bgcolor = null;
            }
            return StyleUtility.mapToImage(map, dims, re, bgcolor);
        } finally {
            if (map != null) {
                map.dispose();
            }
        }
    }

    protected static void populateMap(MapContent map, List<WmsImage> images,
            GeneralEnvelope bounds) {
        for (WmsImage i : images) {
            switch (i.getType()) {
            case COVERAGE:
                map.addLayer(getLayer(i.getCoverage(), i.getStyle()));
                break;
            case FEATURE:
                map.addLayer(getLayer(i.getFeatures(), i.getStyle()));
                break;
            case STYLE_EMBEDDED_FEATURE:
                handleStyledFeatures(map, i.getFeatures());
                break;
            case BLANK:
                // skip
                break;
            default:
                throw new IllegalStateException("Unkown WMS data type: "
                        + i.getType());
            }
        }
    }

    protected static org.geotools.map.Layer getLayer(GridCoverage gridCoverage,
            Style style) {
        if (gridCoverage instanceof GridCoverage2D) {
            org.geotools.map.Layer layer = new GridCoverageLayer(
                    (GridCoverage2D) gridCoverage, style);
            return layer;
        } else {
            // TODO - throw a better exception
            throw new UnsupportedOperationException("GridCoverage2D required");
        }
    }

    protected static org.geotools.map.Layer getLayer(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            Style style) {
        org.geotools.map.Layer layer = new FeatureLayer(featureCollection,
                style);
        return layer;
    }

    /**
     * @param map
     * @param features
     */
    protected static void handleStyledFeatures(MapContent map,
            FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
        SimpleFeature[] simpleFeatures = features
                .toArray(new SimpleFeature[features.size()]);

        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        for (int i = 0; i < simpleFeatures.length; ++i) {
            SimpleFeature feature = simpleFeatures[i];
            Style style = extractStyle(feature, styleFactory);
            MemoryFeatureCollection coll = new MemoryFeatureCollection(
                    feature.getFeatureType());
            coll.add(feature);
            org.geotools.map.Layer layer = getLayer(coll, style);
            map.addLayer(layer);
        }
    }

    protected static Style extractStyle(SimpleFeature feature,
            StyleFactory factory) {
        Object styleObj = feature.getAttribute("Style");
        Style style = null;
        if (styleObj == null) {
            return null;
        }
        if (styleObj instanceof FeatureTypeStyle) {
            style = factory.createStyle();
            style.featureTypeStyles().add((FeatureTypeStyle) styleObj);
        } else if (styleObj instanceof Style) {
            style = (Style) styleObj;
        }
        return style;
    }

    @Deprecated
    protected BufferedImage mergeImages(List<BufferedImage> images,
            boolean clear, Color bgcolor, int width, int height)
            throws WmsException {
        Iterator<BufferedImage> i = images.iterator();
        BufferedImage rval = createBlank(width, height);
        Graphics2D graphics = rval.createGraphics();
        if (!clear) {
            graphics.setColor(bgcolor);
            graphics.fillRect(0, 0, width, height);
        }
        while (i.hasNext()) {
            graphics.drawImage(i.next(), 0, 0, null);
        }
        graphics.dispose();
        return rval;
    }

    protected boolean parseTransparent(Boolean transparent) {
        if (transparent == null) {
            return false;
        }
        return transparent;
    }

    @Override
    public OgcResponse getError(WmsException e, MimeType exceptionFormat) {
        if (exceptionFormat == null) {
            exceptionFormat = OgcResponse.TEXT_XML_MIME;
        }

        String rval = "";
        MimeType mimeType = OgcResponse.TEXT_XML_MIME;
        if (exceptionFormat.equalsIgnoreParams(OgcResponse.TEXT_HTML_MIME)) {
            rval = "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
            rval += "<br>An error occurred performing the request:<br>";
            rval += "<br>Error Code: " + e.getCode().toString();
            rval += "<br>Message: " + e.getMessage() + "</html>";
            mimeType = OgcResponse.TEXT_HTML_MIME;
        } else if (exceptionFormat
                .equalsIgnoreParams(OgcResponse.TEXT_XML_MIME)
                || exceptionFormat
                        .equalsIgnoreParams(OgcResponse.APP_VND_OGC_SE_XML)) {
            rval = wmsExceptionToXml(e);
            mimeType = OgcResponse.TEXT_XML_MIME;
        }
        OgcResponse resp = new OgcResponse(rval, mimeType, TYPE.TEXT);
        switch (e.getCode()) {
        case InternalServerError:
            resp.setError(ErrorType.INT_ERR);
            break;
        case OperationNotSupported:
            resp.setError(ErrorType.NOT_IMPLEMENTED);
            break;
        default:
            resp.setError(ErrorType.BAD_REQ);
        }
        return resp;
    }

    private String wmsExceptionToXml(WmsException e) {
        ServiceExceptionType exType = new ServiceExceptionType();
        exType.setCode(e.getCode().toString());
        exType.setValue(e.getMessage());
        ServiceExceptionReport report = new ServiceExceptionReport();
        report.setServiceException(Arrays.asList(exType));
        String rval = "";
        try {
            JAXBContext context = JAXBContext
                    .newInstance(ServiceExceptionReport.class);
            Marshaller marshaller = context.createMarshaller();

            StringWriter writer = new StringWriter();
            marshaller.marshal(report, writer);
            rval = writer.toString();
        } catch (JAXBException e1) {
            log.error(e1.getLocalizedMessage(), e1);
            return fallbackXmlError(e);
        }
        return rval;
    }

    private String fallbackXmlError(WmsException e) {
        String rval = "";
        rval += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        rval += "<ServiceExceptionReport version=\"1.3.0\" xmlns=\"http://www.opengis.net/ogc\">";
        rval += "<ServiceException code=\"" + e.getCode().toString() + "\">";
        rval += e.getMessage();
        rval += "</ServiceException></ServiceExceptionReport>";
        return rval;
    }

    protected OgcResponse checkGetMapArgs(GetMapRequest req) {
        StyledLayerDescriptor sld = req.getSld();
        String[] layers = req.getLayers();
        boolean sldOnly = sld != null && layers == null;
        OgcResponse rval = null;
        List<String> missing = new LinkedList<String>();
        if (!sldOnly && (layers == null || layers.length < 1)) {
            missing.add(WmsHttpHandler.LAYERS_HEADER);
        }
        String[] styles = req.getStyles();
        if (!sldOnly && (styles == null || styles.length < 1)) {
            missing.add(WmsHttpHandler.STYLES_HEADER);
        }
        String crs = req.getCrs();
        if (crs == null || crs.isEmpty()) {
            missing.add(WmsHttpHandler.CRS_HEADER);
        }
        if (req.getBbox() == null) {
            missing.add(WmsHttpHandler.BBOX_HEADER);
        }
        if (req.getWidth() == null) {
            missing.add(WmsHttpHandler.WIDTH_HEADER);
        }
        if (req.getHeight() == null) {
            missing.add(WmsHttpHandler.HEIGHT_HEADER);
        }
        MimeType format = req.getFormat();
        if (format == null) {
            missing.add(WmsHttpHandler.FORMAT_HEADER);
        }
        if (!missing.isEmpty()) {
            Iterator<String> i = missing.iterator();
            String msg = "Missing the following parameter(s): " + i.next();
            while (i.hasNext()) {
                msg += ", " + i.next();
            }
            WmsException e = new WmsException(Code.MissingParameterValue, msg);
            rval = getError(e, req.getExceptionFormat());
        } else if (!sldOnly && (layers.length != styles.length)) {
            WmsException e = new WmsException(Code.MissingParameterValue,
                    "must have the same number of layers and styles");
            rval = getError(e, req.getExceptionFormat());
        }
        return rval;
    }

    public String getResizeHint() {
        return resizeHint;
    }

    public void setResizeHint(String resizeHint) {
        this.resizeHint = resizeHint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.wms.WmsProvider#getFeatureInfo(com.raytheon.uf.edex
     * .wms.GetFeatureInfoRequest)
     */
    @Override
    public OgcResponse getFeatureInfo(GetFeatureInfoRequest req) {
        GetMapRequest mapReq = req.getMapRequest();
        OgcResponse rval;
        if ((rval = checkGetMapArgs(mapReq)) != null) {
            // problem
            return rval;
        }
        try {
            String[] layers = req.getReqLayers();
            if (layers == null || layers.length < 1) {
                throw new WmsException(Code.MissingParameterValue,
                        "Missing query_layers parameter");
            }
            if (req.getInfoFormat() == null) {
                throw new WmsException(Code.MissingParameterValue,
                        "Missing info_format parameter");
            }
            String[] times = req.getTimes();
            GridGeometry2D geom = createGridGeometry(mapReq);
            Coordinate coord = getCrsCoord(geom, req);
            List<List<SimpleFeature>> features = new ArrayList<List<SimpleFeature>>();
            double scale = getScale(geom);
            for (int i = 0; i < layers.length; ++i) {
                String layer = layers[i];
                String time = times[i];
                WmsSource source = layerManager.getSource(layer);
                List<SimpleFeature> res = source.getFeatureInfo(layer, geom,
                        time, mapReq.getElevation(), mapReq.getDimensions(),
                        coord, scale);
                if (!res.isEmpty()) {
                    features.add(res);
                }
            }
            rval = formatFeatures(features, req.getInfoFormat());
        } catch (WmsException e) {
            rval = getError(e, req.getExceptionFormat());
        }
        return rval;
    }

    public static double getScale(GridGeometry2D geom) {
        Envelope2D env = geom.getEnvelope2D();
        GridEnvelope2D range = geom.getGridRange2D();
        double xscale = Math.abs((double) env.getMaxX() - env.getMinX())
                / range.width;
        double yscale = Math.abs((double) env.getMaxY() - env.getMinY())
                / range.height;
        double average = (xscale + yscale) / 2;
        return average;
    }

    protected OgcResponse formatFeatures(List<List<SimpleFeature>> features,
            MimeType format) throws WmsException {
        SimpleFeatureFormatter formatter = getFormatter(format);
        if (formatter == null) {
            throw new WmsException(Code.InvalidFormat, "Unknown format "
                    + format);
        }
        try {
            return formatter.format(features);
        } catch (java.lang.Exception e) {
            log.error("Problem formatting features", e);
            throw new WmsException(Code.InternalServerError);
        }
    }

    public static SimpleFeatureFormatter getFormatter(MimeType format) {
        ApplicationContext ctx = EDEXUtil.getSpringContext();
        String[] beans = ctx.getBeanNamesForType(SimpleFeatureFormatter.class);
        for (String bean : beans) {
            SimpleFeatureFormatter sff = (SimpleFeatureFormatter) ctx
                    .getBean(bean);
            if (sff.matchesFormat(format)) {
                return sff;
            }
        }
        return null;
    }

    protected Coordinate getCrsCoord(GridGeometry2D geom,
            GetFeatureInfoRequest req) throws WmsException {
        Integer i = req.getI();
        Integer j = req.getJ();
        if (i == null || j == null) {
            throw new WmsException(Code.MissingParameterValue,
                    "Missing I or J parameter");
        }
        try {
            return getCrsCoord(geom, i, j);
        } catch (Throwable e) {
            log.error("Problem getting CRS coordinates", e);
            throw new WmsException(Code.InternalServerError);
        }
    }

    public static Coordinate getCrsCoord(GridGeometry2D geom, Integer i,
            Integer j) throws Throwable {
        MathTransform2D gridToCRS2D = geom.getGridToCRS2D();
        DirectPosition grid = new DirectPosition2D(i, j);
        DirectPosition origCrs = new DirectPosition2D();
        gridToCRS2D.transform(grid, origCrs);
        return new Coordinate(origCrs.getOrdinate(0), origCrs.getOrdinate(1));
    }

    protected GridGeometry2D createGridGeometry(GetMapRequest mapReq)
            throws WmsException {
        CoordinateReferenceSystem crs = parseCrs(mapReq.getCrs());
        Envelope bbox = parseEnvString(mapReq.getBbox(), crs);
        GeneralEnvelope env = new GeneralEnvelope(crs);
        env.setEnvelope(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(),
                bbox.getMaxY());
        GridEnvelope gridRange = new GeneralGridEnvelope(new int[] { 0, 0 },
                new int[] { mapReq.getWidth(), mapReq.getHeight() });
        GridGeometry2D rval = new GridGeometry2D(gridRange, env);
        return rval;
    }

    protected OgcResponse checkGetLegendGraphicArgs(GetLegendGraphicRequest req) {
        StyledLayerDescriptor sld = req.getSld();
        String layer = req.getLayer();
        boolean sldOnly = sld != null && layer == null;
        OgcResponse rval = null;
        List<String> missing = new LinkedList<String>();
        if (!sldOnly && layer == null) {
            missing.add(WmsHttpHandler.LAYER_HEADER);
        }
        MimeType format = req.getFormat();
        if (format == null) {
            missing.add(WmsHttpHandler.FORMAT_HEADER);
        }
        if (!missing.isEmpty()) {
            Iterator<String> i = missing.iterator();
            String msg = "Missing the following parameter(s): " + i.next();
            while (i.hasNext()) {
                msg += ", " + i.next();
            }
            WmsException e = new WmsException(Code.MissingParameterValue, msg);
            rval = getError(e, req.getExceptionFormat());
        }
        return rval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.wms.WmsProvider#getFeatureInfo(com.raytheon.uf.edex
     * .wms.GetFeatureInfoRequest)
     */
    @Override
    public OgcResponse getLegendGraphic(GetLegendGraphicRequest req) {
        OgcResponse rval = null;
        StyledLayerDescriptor sld = req.getSld();
        String layer = req.getLayer();
        String style = req.getStyle();
        String time = req.getTime();
        String elevation = req.getElevation();
        Map<String, String> dimensions = req.getDimensions();
        Integer width = req.getWidth();
        Integer height = req.getHeight();
        boolean sldOnly = sld != null && layer == null;
        if ((rval = checkGetLegendGraphicArgs(req)) != null) {
            // problem
            return rval;
        }
        String username = req.getUserName();
        String[] roles = req.getRoles();
        try {
            BufferedImage legend = null;
            GetLegendProcessor proc = new GetLegendProcessor(layerManager,
                    time, elevation, dimensions, width, height, username, roles);
            if (sld == null) {
                legend = proc.getLegend(layer, style, true);
            } else if (sldOnly) {
                // This should never happen because layers is a required.
                legend = proc.getLegendSld(sld);
            } else {
                // empty datauri assumed non-issue since sld is defined
                legend = proc.getLegendStyleLib(layer, "", style, sld);
            }
            if (!parseTransparent(req.getTransparent())) {
                String bgString = req.getBgcolor();
                legend = GetLegendProcessor.applyBackground(legend,
                        parseColor(bgString));
            }
            rval = new OgcResponse(legend, req.getFormat(), TYPE.IMAGE);
        } catch (WmsException e) {
            rval = getError(e, req.getExceptionFormat());
        }

        return rval;
    }

}
