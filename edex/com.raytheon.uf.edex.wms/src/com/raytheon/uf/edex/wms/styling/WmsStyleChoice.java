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

import org.geotools.styling.Style;

import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;

/**
 * Style configuration settings for rendering Web Map Service imagery
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
public class WmsStyleChoice {

    public enum TYPE {
        GT, UF, NULL
    }

    protected TYPE type;

    protected Style style;

    protected ColorMapParameters cmapParams;

    public WmsStyleChoice(Style style) {
        this.style = style;
        this.type = (style == null ? TYPE.NULL : TYPE.GT);
    }

    public WmsStyleChoice(ColorMapParameters params) {
        this.cmapParams = params;
        this.type = (params == null ? TYPE.NULL : TYPE.UF);
    }

    public TYPE getType() {
        return type;
    }

    public Style getStyle() {
        return style;
    }

    public ColorMapParameters getCmapParams() {
        return cmapParams;
    }

}
