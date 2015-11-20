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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.CaseInsensitiveMap;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.ogc.common.OgcException;
import com.raytheon.uf.edex.ogc.common.OgcException.Code;
import com.raytheon.uf.edex.ogc.common.OgcOperationInfo;
import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.ogc.common.OgcServiceInfo;
import com.raytheon.uf.edex.ogc.common.feature.JsonFeatureFormatter;
import com.raytheon.uf.edex.ogc.common.http.OgcHttpHandler;
import com.raytheon.uf.edex.ogc.common.http.OgcHttpRequest;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;
import com.raytheon.uf.edex.ogc.common.output.ServletOgcResponse;
import com.raytheon.uf.edex.wms.format.GmlFeatureFormatter;
import com.raytheon.uf.edex.wms.format.HtmlFeatureFormatter;
import com.raytheon.uf.edex.wmts.WmtsProvider.WmtsOpType;

/**
 * Handles REST requests for Web Map Tile Service
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
public class WmtsHttpHandler extends OgcHttpHandler {

    public static final String REQUEST_HEADER = "request";

    public static final String VERSION_HEADER = "version";

    public static final String FORMAT_HEADER = "format";

    public static final String LAYER_HEADER = "layer";

    public static final String STYLE_HEADER = "style";

    public static final String TMATRIX_SET_HEADER = "tileMatrixSet";

    public static final String TMATRIX_HEADER = "tileMatrix";

    public static final String TROW_HEADER = "tileRow";

    public static final String TCOL_HEADER = "tileCol";

    public static final String TIME_HEADER = "time";

    public static final String ELEVATION_HEADER = "elevation";

    public static final String MIME_HEADER = "Content-Type";

    public static final String IFORMAT_HEADER = "infoFormat";

    public static final String I_HEADER = "i";

    public static final String J_HEADER = "j";

    public static final String CAP_PARAM = "getcapabilities";

    public static final String MAP_PARAM = "gettile";

    public static final String FEAT_PARAM = "getfeatureinfo";

    public static final String LEG_PARAM = "getlegendgraphic";

    protected final WmtsProvider provider;

    protected static final IUFStatusHandler log = UFStatus
            .getHandler(WmtsHttpHandler.class);

    public WmtsHttpHandler(WmtsProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void handleInternal(OgcHttpRequest ogcRequest) throws Exception {
        IOgcHttpResponse response = new ServletOgcResponse(
                ogcRequest.getResponse());
        if (ogcRequest.isPost()) {
            InputStream is = ogcRequest.getInputStream();
            provider.handlePost(is, response);
            return;
        }
        Map<String, Object> headers = ogcRequest.getHeaders();
        MimeType exceptionFormat = OgcResponse.TEXT_XML_MIME;
        try {
            exceptionFormat = getMimeType(headers, EXCEP_FORMAT_HEADER);
            if (exceptionFormat == null) {
                exceptionFormat = OgcResponse.TEXT_XML_MIME;
            }
            validateExceptionFormat(exceptionFormat);
            Object obj = headers.get(REQUEST_HEADER);

            if (obj == null || !(obj instanceof String)) {
                throw new OgcException(Code.MissingParameterValue,
                        "Missing parameter: " + REQUEST_HEADER);
            }
            String reqName = (String) obj;
            WmtsBaseRequest wmtsReq = buildRequest(reqName,
                    ogcRequest.getRequest(), headers);
            wmtsReq.execute(provider, response);
        } catch (OgcException e) {
            OgcResponse err = handleError(e, exceptionFormat);
            sendResponse(response, err);
        }
    }

    /**
     * @param exceptionFormat
     * @return
     * @throws OgcException
     */
    private void validateExceptionFormat(MimeType exceptionFormat)
            throws OgcException {
        if (!exceptionFormat.equals(OgcResponse.TEXT_HTML_MIME)
                && !exceptionFormat.equals(OgcResponse.TEXT_XML_MIME)
                && !exceptionFormat.equals(OgcResponse.APP_VND_OGC_SE_XML)) {
            throw new OgcException(Code.InvalidParameterValue,
                    "exceptions parameter invalid");
        }
    }

    protected WmtsBaseRequest buildRequest(String reqName,
            HttpServletRequest request, Map<String, Object> headers)
            throws OgcException {
        WmtsBaseRequest req;
        MimeType exceptionFormat = getMimeType(headers, EXCEP_FORMAT_HEADER);
        if (exceptionFormat == null) {
            exceptionFormat = OgcResponse.TEXT_XML_MIME;
        }
        req = getRequest(reqName, request, headers);
        if (req != null) {
            req.setVersion(getString(headers, VERSION_HEADER));
            req.setFormat(getMimeType(headers, FORMAT_HEADER));
            req.setUserName(getString(headers, USER_HEADER));
            req.setRoles(getStringArr(headers, ROLES_HEADER));
            req.setExceptionFormat(exceptionFormat);
        } else {
            throw new OgcException(Code.OperationNotSupported,
                    "No such operation: " + reqName);
        }
        return req;
    }

    protected WmtsBaseRequest getRequest(String reqName,
            HttpServletRequest request, Map<String, Object> headers)
            throws OgcException {
        WmtsBaseRequest req = null;
        if (reqName.equalsIgnoreCase(CAP_PARAM)) {
            req = getBaseRequest(request, headers);
        } else if (reqName.equalsIgnoreCase(MAP_PARAM)) {
            req = parseTileRequest(headers);
        } else if (reqName.equalsIgnoreCase(FEAT_PARAM)) {
            req = getFeatureInfoReq(headers);
        }

        return req;
    }

    protected WmtsBaseRequest getBaseRequest(HttpServletRequest request,
            Map<String, Object> headers) {
        OgcServiceInfo<WmtsOpType> serviceInfo = getServiceInfo(request);
        WmtsBaseRequest req = new WmtsBaseRequest();
        req.setServiceinfo(serviceInfo);
        return req;
    }

    /**
     * @param request
     */
    private OgcServiceInfo<WmtsOpType> getServiceInfo(HttpServletRequest request) {
        int port = request.getServerPort();
        String base = "http://" + request.getServerName();
        if (port != 80) {
            base += ":" + port;
        }
        String wms = base + "/wms?service=wms";
        base += "/wmts?service=wmts";
        OgcServiceInfo<WmtsOpType> rval = new OgcServiceInfo<WmtsOpType>(base);

        OgcOperationInfo<WmtsOpType> cap = new OgcOperationInfo<WmtsOpType>(
                WmtsOpType.GetCapabilities);
        cap.setHttpBaseHostname(request.getServerName());
        cap.setHttpGetRes(base);
        cap.addFormat("text/xml");
        rval.addOperationInfo(cap);

        OgcOperationInfo<WmtsOpType> map = new OgcOperationInfo<WmtsOpType>(
                WmtsOpType.GetTile);
        map.setHttpBaseHostname(request.getServerName());
        map.setHttpGetRes(base);
        map.addFormat("image/gif");
        map.addFormat("image/png");
        // map.addFormat("image/tiff");
        map.addFormat("image/jpeg");
        rval.addOperationInfo(map);

        OgcOperationInfo<WmtsOpType> info = new OgcOperationInfo<WmtsProvider.WmtsOpType>(
                WmtsOpType.GetFeatureInfo);
        info.setHttpBaseHostname(request.getServerName());
        info.setHttpGetRes(base);
        List<String> formats = Arrays.asList(
                GmlFeatureFormatter.mimeType.toString(),
                JsonFeatureFormatter.mimeType.toString(),
                HtmlFeatureFormatter.mimeType.toString());
        info.setFormats(formats);
        rval.addOperationInfo(info);

        OgcOperationInfo<WmtsOpType> legend = new OgcOperationInfo<WmtsOpType>(
                WmtsOpType.GetLegendGraphic);
        legend.setHttpBaseHostname(request.getServerName());
        legend.setHttpGetRes(wms);
        legend.addFormat("image/gif");
        legend.addFormat("image/png");
        legend.addFormat("image/jpeg");
        rval.addOperationInfo(legend);

        return rval;
    }

    /**
     * @param headers
     * @return
     * @throws WmtsException
     */
    protected WmtsGetFeatureInfoRequest getFeatureInfoReq(
            Map<String, Object> headers) throws OgcException {
        // TODO lookup provider based on version
        // String version = getString(headers.get(VERSION_HEADER));
        GetTileRequest mapReq = parseTileRequest(headers);

        MimeType format = getMimeType(headers, IFORMAT_HEADER);
        Integer i = getInt(headers, I_HEADER);
        Integer j = getInt(headers, J_HEADER);
        MimeType exFormat = getMimeType(headers, EXCEP_FORMAT_HEADER);
        WmtsGetFeatureInfoRequest req = new WmtsGetFeatureInfoRequest(mapReq,
                i, j, format);

        req.setExceptionFormat(exFormat);
        return req;
    }

    protected GetTileRequest parseTileRequest(Map<String, Object> headers)
            throws OgcException {
        String layer = getString(headers, LAYER_HEADER);
        String style = getString(headers, STYLE_HEADER);

        String tMatrixSet = getString(headers, TMATRIX_SET_HEADER);
        String tMatrix = getString(headers, TMATRIX_HEADER);
        Integer tRow = getInt(headers, TROW_HEADER);
        Integer tCol = getInt(headers, TCOL_HEADER);

        String time = getString(headers, TIME_HEADER);
        String elevation = getString(headers, ELEVATION_HEADER);
        Map<String, String> dimensions = getTileDimensions(headers);
        return new GetTileRequest(layer, style, tMatrixSet, tMatrix, tRow,
                tCol, time, elevation, dimensions);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getTileDimensions(Map<String, Object> headers) {
        Map<String, String> rval = new CaseInsensitiveMap();
        for (String key : headers.keySet()) {
            if (key.toLowerCase().startsWith("dim_")) {
                String dim = key.substring(4);
                rval.put(dim, (String) headers.get(key));
            }
        }
        return rval;
    }

    protected OgcResponse handleError(OgcException e, MimeType exceptionFormat) {
        return provider.getError(new WmtsException(e), exceptionFormat);
    }

}
