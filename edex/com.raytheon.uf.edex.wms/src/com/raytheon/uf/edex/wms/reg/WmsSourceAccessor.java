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

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * Controls access to OGC Web Map Service plugin adapter objects
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
public class WmsSourceAccessor {

    public synchronized Map<String, WmsSource> getSources() {
        Map<String, WmsSource> sources = new HashMap<String, WmsSource>();
        ApplicationContext ctx = EDEXUtil.getSpringContext();
        String[] beans = ctx.getBeanNamesForType(WmsSource.class);
        for (String bean : beans) {
            WmsSource s = (WmsSource) ctx.getBean(bean);
            sources.put(s.getKey(), s);
        }
        return sources;
    }

}
