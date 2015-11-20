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
package com.raytheon.uf.edex.wms.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.styling.NamedLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayer;
import org.geotools.styling.StyledLayerDescriptor;

/**
 * Wraps a Styled Layer Descriptor object and provides indexed access to style
 * information
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
public class StyleLibrary {

    protected Map<String, Style> layerDefaults;

    protected Map<String, Style> allStyles;

    public StyleLibrary(StyledLayerDescriptor sld) {
        StyledLayer[] layers = sld.getStyledLayers();
        layerDefaults = new HashMap<String, Style>(layers.length);
        allStyles = new HashMap<String, Style>();
        for (StyledLayer sl : layers) {
            NamedLayer layer = (NamedLayer) sl;
            Style[] styles = layer.getStyles();
            if (styles.length == 1) {
                layerDefaults.put(layer.getName(), styles[0]);
            }
            for (Style s : styles) {
                if (s.isDefault()) {
                    layerDefaults.put(layer.getName(), s);
                }
                allStyles.put(s.getName(), s);
            }
        }
    }

    public Style getAny() {
        if (allStyles.isEmpty()) {
            return null;
        }
        return allStyles.values().iterator().next();
    }

    public Set<String> getAllStyles() {
        return allStyles.keySet();
    }

    public Style getDefault(String layer) {
        return layerDefaults.get(layer);
    }

    public Style getNamedStyle(String name) {
        return allStyles.get(name);
    }
}
