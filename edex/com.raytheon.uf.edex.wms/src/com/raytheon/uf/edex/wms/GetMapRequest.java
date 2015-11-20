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
package com.raytheon.uf.edex.wms;

import java.util.Map;

import org.geotools.styling.StyledLayerDescriptor;

import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.wms.IWmsProvider.WmsOpType;

/**
 * Represents an OGC Web Map Service get map request
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
public class GetMapRequest extends BaseRequest<WmsOpType> {

    protected String[] layers;

    protected String[] styles;

    protected String crs;

    protected String bbox;

    protected Integer width;

    protected Integer height;

    protected Boolean transparent;

    protected String bgcolor;

    protected String[] times;

    protected String elevation;

    protected Map<String, String> dimensions;

    protected StyledLayerDescriptor sld;

    public GetMapRequest() {
    }

    public GetMapRequest(GetMapRequest req) {
        super(req.getVersion(), req.getFormat(), req.getUserName(), req
                .getRoles());
        this.layers = req.getLayers();
        this.styles = req.getStyles();
        this.crs = req.getCrs();
        this.crs = req.crs;
        this.bbox = req.bbox;
        this.width = req.width;
        this.height = req.height;
        this.transparent = req.transparent;
        this.bgcolor = req.bgcolor;
        this.times = req.times;
        this.elevation = req.elevation;
        this.dimensions = req.dimensions;
        this.sld = req.sld;
    }

    public GetMapRequest(String[] layers, String[] styles, String crs,
            String bbox, Integer width, Integer height, Boolean transparent,
            String bgcolor, String[] times, String elevation,
            Map<String, String> dimensions, StyledLayerDescriptor sld) {
        super();
        this.layers = layers;
        this.styles = styles;
        this.crs = crs;
        this.bbox = bbox;
        this.width = width;
        this.height = height;
        this.transparent = transparent;
        this.bgcolor = bgcolor;
        this.times = times;
        this.elevation = elevation;
        this.dimensions = dimensions;
        this.sld = sld;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.wms.BaseRequest#execute(com.raytheon.uf.edex.wms
     * .WmsProvider)
     */
    @Override
    public OgcResponse execute(IWmsProvider provider) {
        return provider.getMap(this);
    }

    public String[] getLayers() {
        return layers;
    }

    public void setLayers(String[] layers) {
        this.layers = layers;
    }

    public String[] getStyles() {
        return styles;
    }

    public void setStyles(String[] styles) {
        this.styles = styles;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Boolean getTransparent() {
        return transparent;
    }

    public void setTransparent(Boolean transparent) {
        this.transparent = transparent;
    }

    public String getBgcolor() {
        return bgcolor;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public String[] getTimes() {
        return times;
    }

    public void setTimes(String[] times) {
        this.times = times;
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, String> dimensions) {
        this.dimensions = dimensions;
    }

    public StyledLayerDescriptor getSld() {
        return sld;
    }

    public void setSld(StyledLayerDescriptor sld) {
        this.sld = sld;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getBbox() {
        return bbox;
    }

    public void setBbox(String bbox) {
        this.bbox = bbox;
    }

}
