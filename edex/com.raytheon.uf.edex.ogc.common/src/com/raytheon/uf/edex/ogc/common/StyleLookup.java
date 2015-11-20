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
package com.raytheon.uf.edex.ogc.common;

import java.util.Collection;

/**
 * Styling interface for retrieving style information for layers
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 29, 2012            bclement     Initial creation
 * Nov 19, 2015 5087       bclement     return Collection instead of list
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public interface StyleLookup {

    /**
     * @param layername
     * @return the name of the default style for layer
     */
    public String lookup(String layername);

    /**
     * @return all styles
     */
    public Collection<OgcStyle> getStyles();

    /**
     * Set class loader to use when locating resources
     * 
     * @param loader
     */
    public void setLoader(ClassLoader loader);

}
