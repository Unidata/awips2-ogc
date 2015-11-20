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
package com.raytheon.uf.edex.wms.reg;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Represents data that can be rendered to an image
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
public class WmsImage {

    public enum TYPE {
        COVERAGE, FEATURE, STYLE_EMBEDDED_FEATURE, BLANK
    };

    protected TYPE type;

    protected GridCoverage2D coverage;

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> features;

    protected Style style;

    public WmsImage(GridCoverage2D coverage, Style style) {
        this(coverage);
        this.style = style;
    }

    public WmsImage(GridCoverage2D coverage) {
        this.coverage = coverage;
        this.type = coverage == null ? TYPE.BLANK : TYPE.COVERAGE;
    }

    public WmsImage(
            FeatureCollection<SimpleFeatureType, SimpleFeature> features,
            Style style) {
        this.features = features;
        this.style = style;
        this.type = features == null ? TYPE.BLANK : TYPE.FEATURE;
    }

    public WmsImage(FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
        this.features = features;
        this.type = features == null ? TYPE.BLANK : TYPE.STYLE_EMBEDDED_FEATURE;
    }

    public TYPE getType() {
        return type;
    }

    public GridCoverage2D getCoverage() {
        return coverage;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures() {
        return features;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
        this.type = TYPE.FEATURE;
    }

}
