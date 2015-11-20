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
package com.raytheon.uf.edex.ogc.common.spatial;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Stores a simple well known text CRS definition linked to a URN ID
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2015 5087       bclement    Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Entity
@Table(name = "defined_crs")
@DynamicSerialize
public class DefinedCrs extends PersistableDataObject<String> {

    private static final long serialVersionUID = -6844234924108827628L;

    @Id
    @Column
    @DynamicSerializeElement
    @XmlAttribute(required = true)
    private String id;

    @Column(length = 2047)
    @DynamicSerializeElement
    @XmlElement(required = true)
    private String wkt;

    private transient CoordinateReferenceSystem crs;

    /**
     * 
     */
    public DefinedCrs() {
    }

    /**
     * @param id
     * @param wkt
     */
    public DefinedCrs(String id, String wkt) {
        this.id = id;
        this.wkt = wkt;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the wkt
     */
    public String getWkt() {
        if (wkt == null && crs != null) {
            wkt = crs.toWKT();
        }
        return wkt;
    }

    /**
     * @param wkt
     *            the wkt to set
     */
    public void setWkt(String wkt) {
        this.wkt = wkt;
        this.crs = null;
    }

    /**
     * @param crs
     *            the crs to set
     */
    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
        this.wkt = crs.toWKT();
    }

    /**
     * @return the crs
     */
    public CoordinateReferenceSystem getCrs() {
        if (crs == null && wkt != null) {
            try {
                crs = CRS.parseWKT(wkt);
            } catch (FactoryException e) {
                throw new IllegalStateException("Invalid CRS WKT: " + wkt, e);
            }
        }
        return crs;
    }

}
