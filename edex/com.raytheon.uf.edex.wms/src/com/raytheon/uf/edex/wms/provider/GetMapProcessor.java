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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayer;
import org.geotools.styling.StyledLayerDescriptor;

import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;
import com.raytheon.uf.edex.wms.reg.WmsImage;
import com.raytheon.uf.edex.wms.reg.WmsSource;
import com.raytheon.uf.edex.wms.util.StyleLibrary;

/**
 * Handles get map requests for layer imagery
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
public class GetMapProcessor {

    protected GridGeometry2D geometry;

    protected String elevation;

    protected Map<String, String> dimensions;

    protected String username;

    protected Set<String> roles;

    protected WmsLayerManager layerManager;

    protected double scale;

    public GetMapProcessor(WmsLayerManager layerManager,
            GridGeometry2D geometry, String elevation,
            Map<String, String> dimensions, double scale, String username,
            String[] roles) {
        this(layerManager, geometry, elevation, dimensions, scale);
        this.username = username;
        this.roles = getAsSet(roles);
    }

    public GetMapProcessor(WmsLayerManager layerManager,
            GridGeometry2D geometry, String elevation,
            Map<String, String> dimensions, double scale) {
        super();
        this.layerManager = layerManager;
        this.geometry = geometry;
        this.elevation = elevation;
        this.dimensions = dimensions;
        this.scale = scale;
    }

    public List<WmsImage> getMapSld(StyledLayerDescriptor sld, String[] times)
            throws WmsException {
        StyledLayer[] layers = sld.getStyledLayers();
        ArrayList<WmsImage> rval = new ArrayList<WmsImage>(layers.length);
        String time;
        if (times.length == 1) {
            time = times[0];
        } else {
            throw new WmsException(Code.InvalidParameterValue,
                    "times per layer not supported for sld");
        }
        for (StyledLayer sl : layers) {
            if (sl instanceof NamedLayer) {
                NamedLayer layer = (NamedLayer) sl;
                String layerName = layer.getName();
                WmsSource source = getSource(layerName);
                Style[] styles = layer.getStyles();
                if (styles == null || styles.length < 1) {
                    // request a layer with default style
                    WmsImage img = source.getImage(layerName, null, true,
                            geometry, time, elevation, dimensions, scale);
                    rval.add(img);
                } else {
                    for (Style s : styles) {
                        WmsImage img = source.getImage(layerName, null, false,
                                geometry, time, elevation, dimensions, scale);
                        img.setStyle(s);
                        rval.add(img);
                    }
                }
            }
        }
        return rval;
    }

    protected WmsSource getSource(String layer) throws WmsException {
        return layerManager.getSource(layer);
    }

    protected Set<String> getAsSet(String[] strs) {
        Set<String> rval = null;
        if (strs != null) {
            rval = new HashSet<String>(Arrays.asList(strs));
        }
        return rval;
    }

    public List<WmsImage> getMapStyleLib(String[] layers, String[] styles,
            String[] times, StyledLayerDescriptor sld) throws WmsException {
        StyleLibrary lib = new StyleLibrary(sld);
        ArrayList<WmsImage> rval = new ArrayList<WmsImage>(layers.length);
        for (int i = 0; i < layers.length; ++i) {
            String layerName = layers[i];
            WmsSource source = getSource(layerName);
            String styleName = styles[i];
            String time = times[i];
            Style style = null;
            if (styleName != null && styleName.trim().isEmpty()) {
                // use default
                style = lib.getDefault(layerName);
            } else {
                // use library
                style = lib.getNamedStyle(styleName);
            }
            WmsImage img;
            if (style == null) {
                // not in library, pass to source to see if they know it
                img = source.getImage(layerName, styleName, false, geometry,
                        time, elevation, dimensions, scale);
            } else {
                // get without style
                img = source.getImage(layerName, null, false, geometry, time,
                        elevation, dimensions, scale);
                img.setStyle(style);
            }
            rval.add(img);
        }
        return rval;
    }

    public List<WmsImage> getMap(String[] layers, String[] styles,
            String[] times) throws WmsException {
        ArrayList<WmsImage> rval = new ArrayList<WmsImage>(layers.length);
        for (int i = 0; i < layers.length; ++i) {
            String layerName = layers[i];
            WmsSource source = getSource(layerName);
            String styleName = styles[i];
            String time = times[i];
            boolean defaultStyle = (styleName == null || styleName.isEmpty());
            WmsImage img = source.getImage(layerName, styleName, defaultStyle,
                    geometry, time, elevation, dimensions, scale);
            if (img != null) {
                rval.add(img);
            }
        }
        return rval;
    }

}
