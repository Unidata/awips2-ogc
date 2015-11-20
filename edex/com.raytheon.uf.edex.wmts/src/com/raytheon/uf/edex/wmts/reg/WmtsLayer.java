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
package com.raytheon.uf.edex.wmts.reg;

import java.util.Arrays;
import java.util.Collection;

import com.raytheon.uf.edex.ogc.common.OgcBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcDimension;
import com.raytheon.uf.edex.ogc.common.OgcGeoBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcLayer;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.wmts.tiling.TileMatrixRegistry;

/**
 * Metadata object representing a Web Map Tile Service layer. This is a
 * simplified form of the Web Map Service layer object.
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
public class WmtsLayer {

    protected String identifier;

    protected String title;

    protected Collection<String> keywords;

    protected OgcGeoBoundingBox geoBoundingBox;

    protected Collection<OgcStyle> styles;

    protected Collection<OgcDimension> dimensions;

    protected Collection<String> tileMatrixSets;

    protected Collection<OgcBoundingBox> bboxes;

    public WmtsLayer() {
    }

    public WmtsLayer(OgcLayer layer) {
        this(layer.getName(), layer.getFullTitle(), layer.getKeywords(), layer
                .getGeoBoundingBox(), layer.getStyles(), layer.getDimensions(),
                Arrays.asList(TileMatrixRegistry.crs84TSetName,
                        TileMatrixRegistry.googleTSetName), layer
                        .getBoundingBox());
    }

    /**
     * @param identifier
     * @param title
     * @param keywords
     * @param geoBoundingBox
     * @param styles
     * @param dimensions
     * @param tileMatrixSets
     */
    public WmtsLayer(String identifier, String title,
            Collection<String> keywords, OgcGeoBoundingBox geoBoundingBox,
            Collection<OgcStyle> styles, Collection<OgcDimension> dimensions,
            Collection<String> tileMatrixSets, Collection<OgcBoundingBox> bboxes) {
        this.identifier = identifier;
        this.title = title;
        this.keywords = keywords;
        this.geoBoundingBox = geoBoundingBox;
        this.styles = styles;
        this.dimensions = dimensions;
        this.tileMatrixSets = tileMatrixSets;
        this.bboxes = bboxes;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the keywords
     */
    public Collection<String> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            the keywords to set
     */
    public void setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * @return the geoBoundingBox
     */
    public OgcGeoBoundingBox getGeoBoundingBox() {
        return geoBoundingBox;
    }

    /**
     * @param geoBoundingBox
     *            the geoBoundingBox to set
     */
    public void setGeoBoundingBox(OgcGeoBoundingBox geoBoundingBox) {
        this.geoBoundingBox = geoBoundingBox;
    }

    /**
     * @return the styles
     */
    public Collection<OgcStyle> getStyles() {
        return styles;
    }

    /**
     * @param styles
     *            the styles to set
     */
    public void setStyles(Collection<OgcStyle> styles) {
        this.styles = styles;
    }

    /**
     * @return the dimensions
     */
    public Collection<OgcDimension> getDimensions() {
        return dimensions;
    }

    /**
     * @param dimensions
     *            the dimensions to set
     */
    public void setDimensions(Collection<OgcDimension> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * @return the tileMatrixSets
     */
    public Collection<String> getTileMatrixSets() {
        return tileMatrixSets;
    }

    /**
     * @param tileMatrixSets
     *            the tileMatrixSets to set
     */
    public void setTileMatrixSets(Collection<String> tileMatrixSets) {
        this.tileMatrixSets = tileMatrixSets;
    }

    /**
     * @return the bboxes
     */
    public Collection<OgcBoundingBox> getBboxes() {
        return bboxes;
    }

    /**
     * @param bboxes
     *            the bboxes to set
     */
    public void setBboxes(Collection<OgcBoundingBox> bboxes) {
        this.bboxes = bboxes;
    }

}
