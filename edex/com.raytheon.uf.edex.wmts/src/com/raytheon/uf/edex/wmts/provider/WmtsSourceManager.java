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
package com.raytheon.uf.edex.wmts.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.ogc.common.OgcLayer;
import com.raytheon.uf.edex.wms.reg.WmsSource;
import com.raytheon.uf.edex.wms.reg.WmsSourceAccessor;
import com.raytheon.uf.edex.wmts.reg.WmtsSource;
import com.raytheon.uf.edex.wmts.statictile.FileStaticWmtsSource;

/**
 * Manages access to Web Map Tile Service source objects
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
public class WmtsSourceManager {

    protected static final IUFStatusHandler log = UFStatus
            .getHandler(WmtsSourceManager.class);

    protected static final String staticDirectory = System.getProperty(
            "wmts.static.directory", "wmts-static");

    protected WmsSourceAccessor sourceAccessor = new WmsSourceAccessor();

    private Map<String, WmtsSource> _sourceMap;

    private final Object _mutex = new Object();

    private long lastUpdate = 0l;

    private int cacheTtlMilliSeconds = 30 * 1000;

    public WmtsSourceManager() {
    }

    protected Map<String, WmtsSource> getMap() {
        if (_sourceMap == null || cacheTimeout()) {
            synchronized (_mutex) {
                if (_sourceMap == null || cacheTimeout()) {
                    _sourceMap = getWmsSources();
                    addFileSystemSources(_sourceMap);
                    addOtherWmtsSources(_sourceMap);
                    lastUpdate = System.currentTimeMillis();
                }
            }
        }
        return _sourceMap;
    }

    private Map<String, WmtsSource> getWmsSources() {
        Map<String, WmsSource> sources = sourceAccessor.getSources();
        Map<String, WmtsSource> rval = new HashMap<String, WmtsSource>(
                sources.size());
        for (WmsSource source : sources.values()) {
            if (source.isWmtsCapable()) {
                rval.put(source.getKey(), new WmsSourceAdapter(source));
            }
        }
        return rval;
    }

    private void addFileSystemSources(Map<String, WmtsSource> target) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        File topStatic = pathMgr.getStaticFile(LocalizationType.EDEX_STATIC,
                staticDirectory);
        if (topStatic != null && topStatic.exists() && topStatic.isDirectory()
                && topStatic.canRead()) {
            for (File f : topStatic.listFiles()) {
                if (f.isDirectory() && f.canRead()) {
                    FileStaticWmtsSource source = new FileStaticWmtsSource(f);
                    target.put(source.getKey(), source);
                }
            }
        }
    }

    private void addOtherWmtsSources(Map<String, WmtsSource> target) {
        ApplicationContext ctx = EDEXUtil.getSpringContext();
        String[] beans = ctx.getBeanNamesForType(WmtsSource.class);
        for (String bean : beans) {
            WmtsSource s = (WmtsSource) ctx.getBean(bean);
            target.put(s.getKey(), s);
        }
    }

    private boolean cacheTimeout() {
        long milliDiff = System.currentTimeMillis() - lastUpdate;
        return milliDiff > cacheTtlMilliSeconds;
    }

    public WmtsSource getSource(String layer) {
        String key = OgcLayer.getKey(layer);
        return getMap().get(key);
    }

    public List<WmtsSource> getSources() {
        Map<String, WmtsSource> map = getMap();
        return new ArrayList<WmtsSource>(map.values());
    }

    /**
     * @return the cacheTtlMilliSeconds
     */
    public int getCacheTtlMilliSeconds() {
        return cacheTtlMilliSeconds;
    }

    /**
     * @param cacheTtlMilliSeconds
     *            the cacheTtlMilliSeconds to set
     */
    public void setCacheTtlMilliSeconds(int cacheTtlMilliSeconds) {
        this.cacheTtlMilliSeconds = cacheTtlMilliSeconds;
    }

}
