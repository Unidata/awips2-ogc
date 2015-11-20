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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.ogc.common.OgcException;

/**
 * Authority for user-defined coordinate reference systems that are registered
 * in EDEX
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
public interface IDefinedCrsAuthority {

    /**
     * Store WKT from input stream
     * 
     * @param urn
     * @param in
     * @throws DataAccessLayerException
     * @throws IOException
     */
    public void storeStream(String urn, InputStream in)
            throws DataAccessLayerException, IOException;

    /**
     * Store WKT using URN as key
     * 
     * @param urn
     * @param wkt
     * @throws DataAccessLayerException
     */
    public void store(String urn, String wkt) throws DataAccessLayerException;

    /**
     * Treat url like it was a urn to this service and lookup the CRS for the
     * provided URN (if found)
     * 
     * @param url
     * @return null if none found
     * @throws URISyntaxException
     * @throws OgcException
     */
    public CoordinateReferenceSystem resolve(URL url)
            throws URISyntaxException, OgcException;

    /**
     * Lookup CRS using URN
     * 
     * @param URN
     * @return null if none found
     * @throws OgcException
     */
    public CoordinateReferenceSystem lookup(String URN) throws OgcException;

    /**
     * @param URN
     * @return the WKT for the CRS or null if not found
     * @throws OgcException
     */
    public String getWKT(String URN) throws OgcException;

    /**
     * Format URN list to string, defaults to HTML if format isn't provided or
     * supported
     * 
     * @param format
     * @return
     * @throws OgcException
     */
    public String formatUrns(String format) throws OgcException;

    /**
     * @return list of defined URNs
     * @throws OgcException
     */
    public List<String> getUrns() throws OgcException;

    /**
     * Remove CRS definition at URN
     * 
     * @param urn
     * @throws DataAccessLayerException
     * @throws OgcException
     */
    public void remove(String urn) throws DataAccessLayerException,
            OgcException;

}
