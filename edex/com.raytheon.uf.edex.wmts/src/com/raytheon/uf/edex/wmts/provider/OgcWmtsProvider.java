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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.wms.v_1_3_0.ServiceExceptionReport;
import net.opengis.wms.v_1_3_0.ServiceExceptionType;

import org.opengis.feature.simple.SimpleFeature;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ogc.common.OgcNamespace;
import com.raytheon.uf.edex.ogc.common.OgcPrefix;
import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.ogc.common.OgcResponse.ErrorType;
import com.raytheon.uf.edex.ogc.common.OgcResponse.TYPE;
import com.raytheon.uf.edex.ogc.common.feature.JsonFeatureFormatter;
import com.raytheon.uf.edex.ogc.common.feature.SimpleFeatureFormatter;
import com.raytheon.uf.edex.ogc.common.jaxb.OgcJaxbManager;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;
import com.raytheon.uf.edex.ogc.common.output.OgcResponseOutput;
import com.raytheon.uf.edex.wms.format.HtmlFeatureFormatter;
import com.raytheon.uf.edex.wms.provider.OgcWmsProvider;
import com.raytheon.uf.edex.wmts.GetTileRequest;
import com.raytheon.uf.edex.wmts.WmtsBaseRequest;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;
import com.raytheon.uf.edex.wmts.WmtsGetFeatureInfoRequest;
import com.raytheon.uf.edex.wmts.WmtsHttpHandler;
import com.raytheon.uf.edex.wmts.WmtsProvider;
import com.raytheon.uf.edex.wmts.cache.TileCacheManager;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixRegistry;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Web Map Tile Service version 1.0.0 implementation
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
public class OgcWmtsProvider implements WmtsProvider {

    protected CapabilitiesBuilder capBuilder;

    private OgcJaxbManager jaxbManager;

    protected static final IUFStatusHandler log = UFStatus
            .getHandler(OgcWmtsProvider.class);

    protected final WmtsSourceManager sourceManager;

    protected TileFetcher tileFetcher;

    protected FeatureInfoFetcher featureFetcher;

    protected TileCacheManager cache;

    protected static final Map<String, String> NS_MAP = new ConcurrentHashMap<String, String>();

    static {
        NS_MAP.put(OgcNamespace.EDEX, OgcPrefix.EDEX);
        NS_MAP.put(OgcNamespace.GML, OgcPrefix.GML);
        NS_MAP.put(OgcNamespace.OGC, OgcPrefix.OGC);
        NS_MAP.put(OgcNamespace.OWS110, OgcPrefix.OWS);
        NS_MAP.put(OgcNamespace.WMTS100, OgcPrefix.WMTS);
        NS_MAP.put(OgcNamespace.XSI, OgcPrefix.XSI);
        NS_MAP.put(OgcNamespace.XLINK, OgcPrefix.XLINK);
    }

    protected Class<?>[] jaxbClasses = new Class<?>[] {
            net.opengis.wmts.v_1_0_0.ObjectFactory.class,
            net.opengis.ows.v_1_1_0.ObjectFactory.class };

    /**
     * @param layerManager
     * @param jaxbManager
     * @throws JAXBException
     */
    public OgcWmtsProvider(TileMatrixRegistry registry, TileCacheManager cache,
            WmtsSourceManager sourceManager) throws JAXBException {
        this.sourceManager = sourceManager;
        NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
            @Override
            public String getPreferredPrefix(String uri, String suggestion,
                    boolean requirePrefix) {
                return NS_MAP.get(uri);
            }
        };
        this.jaxbManager = new OgcJaxbManager(mapper, jaxbClasses);

        // TODO these should be queried for
        List<String> imageFormats = Arrays.asList("image/png", "image/jpeg");
        List<String> infoFormats = Arrays.asList(
                JsonFeatureFormatter.mimeType.toString(),
                HtmlFeatureFormatter.mimeType.toString());
        this.capBuilder = new CapabilitiesBuilder(this.jaxbManager,
                imageFormats, infoFormats, registry, this.sourceManager);
        this.tileFetcher = new TileFetcher(registry, sourceManager, cache);
        this.featureFetcher = new FeatureInfoFetcher(registry, sourceManager);
    }

    @Override
    public void getCapabilities(WmtsBaseRequest req, IOgcHttpResponse response) {
        try {
            capBuilder.build(req, response);
        } catch (WmtsException e) {
            sendError(req.getExceptionFormat(), e, response);
        }

    }

    protected void sendError(MimeType format, WmtsException e,
            IOgcHttpResponse response) {
        OgcResponse error = getError(e, format);
        try {
            OgcResponseOutput.output(error, response);
        } catch (Exception e1) {
            log.error("Unable to send error response", e1);
        }
    }

    @Override
    public void getTile(GetTileRequest req, IOgcHttpResponse response) {
        try {
            validate(req);
            tileFetcher.fulfill(req, response);
        } catch (WmtsException e) {
            sendError(req.getExceptionFormat(), e, response);
        }
    }

    protected void validate(GetTileRequest req) throws WmtsException {
        List<String> missing = new ArrayList<String>();
        checkStr(req.getLayer(), WmtsHttpHandler.LAYER_HEADER, missing);
        checkStr(req.getStyle(), WmtsHttpHandler.STYLE_HEADER, missing);
        checkObj(req.getFormat(), WmtsHttpHandler.FORMAT_HEADER, missing);
        checkStr(req.gettMatrixSet(), WmtsHttpHandler.TMATRIX_SET_HEADER,
                missing);
        checkStr(req.gettMatrix(), WmtsHttpHandler.TMATRIX_HEADER, missing);
        checkObj(req.gettRow(), WmtsHttpHandler.TROW_HEADER, missing);
        checkObj(req.gettCol(), WmtsHttpHandler.TCOL_HEADER, missing);
        if (!missing.isEmpty()) {
            Iterator<String> i = missing.iterator();
            String msg = "Missing the following parameter(s): " + i.next();
            while (i.hasNext()) {
                msg += ", " + i.next();
            }
            throw new WmtsException(Code.MissingParameterValue, msg);
        }
        Iterator<?> it = ImageIO.getImageWritersByMIMEType(req.getFormat()
                .toStringWithoutParams());
        if (!it.hasNext()) {
            throw new WmtsException(Code.InvalidParameterValue,
                    "Format not supported: " + req.getFormat());
        }
    }

    protected void checkStr(String str, String header, List<String> missing) {
        if (str == null || str.isEmpty()) {
            missing.add(header);
        }
    }

    protected void checkObj(Object o, String header, List<String> missing) {
        if (o == null) {
            missing.add(header);
        }
    }

    @Override
    public OgcResponse getError(WmtsException e, MimeType exceptionFormat) {
        if (exceptionFormat == null) {
            exceptionFormat = OgcResponse.TEXT_HTML_MIME;
        }

        String rval = "";
        MimeType mimeType = OgcResponse.TEXT_HTML_MIME;
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
            rval = exceptionToXml(e);
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

    private String exceptionToXml(WmtsException e) {
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

    private String fallbackXmlError(WmtsException e) {
        String rval = "";
        rval += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        rval += "<ServiceExceptionReport version=\"1.3.0\" xmlns=\"http://www.opengis.net/ogc\">";
        rval += "<ServiceException code=\"" + e.getCode().toString() + "\">";
        rval += e.getMessage();
        rval += "</ServiceException></ServiceExceptionReport>";
        return rval;
    }

    @Override
    public void handlePost(InputStream in, IOgcHttpResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        sendError((MimeType) null,
                new WmtsException(Code.OperationNotSupported), response);
    }

    @Override
    public void getFeatureInfo(WmtsGetFeatureInfoRequest req,
            IOgcHttpResponse response) {
        try {
            validate(req);
            Integer i = req.getI();
            Integer j = req.getJ();
            if (i == null || j == null) {
                throw new WmtsException(Code.MissingParameterValue,
                        "Missing i or j parameters");
            }
            MimeType infoFormat = req.getInfoFormat();
            if (infoFormat == null) {
                throw new WmtsException(Code.MissingParameterValue,
                        "Missing info format parameter");
            }
            SimpleFeatureFormatter formatter = OgcWmsProvider
                    .getFormatter(infoFormat);
            if (formatter == null) {
                throw new WmtsException(Code.InvalidParameterValue,
                        "Invalid info format: " + infoFormat);
            }
            List<SimpleFeature> features = featureFetcher.fetch(req);
            OgcResponse resp;
            try {
                List<List<SimpleFeature>> arg = new ArrayList<List<SimpleFeature>>(
                        1);
                arg.add(features);
                resp = formatter.format(arg);
            } catch (Exception e) {
                log.error("Problem formatting feature info", e);
                throw new WmtsException(Code.InternalServerError);
            }
            try {
                OgcResponseOutput.output(resp, response);
            } catch (Exception e) {
                log.error("Unable to send response", e);
            }
        } catch (WmtsException e) {
            sendError(req.getExceptionFormat(), e, response);
        }
    }

}
