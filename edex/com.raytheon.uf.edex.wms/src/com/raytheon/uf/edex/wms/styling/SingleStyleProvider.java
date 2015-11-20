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
package com.raytheon.uf.edex.wms.styling;

import java.util.Arrays;
import java.util.List;

import org.geotools.styling.Style;

import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.StyleLookup;

/**
 * Abstract class for styling data that only has one valid style
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
public abstract class SingleStyleProvider implements StyleLookup {

    protected OgcStyle styleInfo;

    protected Style style;

    public SingleStyleProvider(Style style) {
        this(new OgcStyle("default"), style);
    }

    public SingleStyleProvider(OgcStyle styleInfo, Style style) {
        this.styleInfo = styleInfo;
        this.style = style;
    }

    @Override
    public String lookup(String layername) {
        return styleInfo.getName();
    }

    @Override
    public List<OgcStyle> getStyles() {
        return Arrays.asList(styleInfo);
    }

    @Override
    public void setLoader(ClassLoader loader) {
        // this class doesn't look anything up on the resource path
    }

}
