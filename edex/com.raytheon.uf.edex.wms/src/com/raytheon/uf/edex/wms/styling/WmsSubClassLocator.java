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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.raytheon.uf.common.serialization.reflect.ISubClassLocator;
import com.raytheon.uf.common.style.ParamLevelMatchCriteria;
import com.raytheon.uf.common.style.arrow.ArrowPreferences;
import com.raytheon.uf.common.style.contour.ContourPreferences;
import com.raytheon.uf.common.style.graph.GraphPreferences;
import com.raytheon.uf.common.style.image.ImagePreferences;
import com.raytheon.uf.common.style.level.RangeLevel;
import com.raytheon.uf.common.style.level.SingleLevel;

/**
 * Utility to locate JAXB subclasses for styling classes
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
class WmsSubClassLocator implements ISubClassLocator {

    @Override
    public Collection<Class<?>> locateSubClasses(Class<?> base) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        // classes.add(AbstractStylePreferences.class);
        classes.add(ArrowPreferences.class);
        classes.add(ContourPreferences.class);
        classes.add(GraphPreferences.class);
        classes.add(ImagePreferences.class);

        classes.add(ParamLevelMatchCriteria.class);
        // classes.add(MatchCriteria.class);

        classes.add(RangeLevel.class);
        classes.add(SingleLevel.class);
        return classes;
    }

    @Override
    public void save() {

    }

}