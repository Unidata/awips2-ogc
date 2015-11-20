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
package com.raytheon.uf.edex.wmts.cache;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tile cache that keeps tile in memory
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 23, 2015 5087       bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class MemoryTileCache extends TileCacheManager {

    // a 256x256 tile is about 256K, 64 of them is about 16M
    protected static final int CACHE_SIZE = Integer.getInteger(
            "wmts.tile.cache.size", 64);

    protected final Map<String, byte[]> cache = new LinkedHashMap<String, byte[]>(
            CACHE_SIZE, 0.75f, true) {
        private static final long serialVersionUID = -7440658902899362316L;

        @Override
        protected boolean removeEldestEntry(Entry<String, byte[]> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    @Override
    protected byte[] read(String key) throws IOException {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Override
    protected void write(String key, byte[] arr) throws IOException {
        synchronized (cache) {
            cache.put(key, arr);
        }
    }

    @Override
    public void remove(String key) {
        synchronized (cache) {
            cache.remove(key);
        }
    }

}
