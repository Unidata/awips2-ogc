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
package com.raytheon.uf.edex.plugin.grid.ogc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.PluginProperties;
import com.raytheon.uf.common.dataplugin.grid.GridRecord;
import com.raytheon.uf.edex.database.query.DatabaseQuery;
import com.raytheon.uf.edex.ogc.common.OgcException;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;
import com.raytheon.uf.edex.wms.reg.DefaultWmsSource;
import com.raytheon.uf.edex.wms.styling.ColormapStyleProvider;
import com.raytheon.uf.edex.wms.styling.ICoverageStyleProvider;

/**
 * Provides access to gridded data to the OGC Web Map Service
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 6, 2013             bclement     Initial creation
 * 
 * </pre>
 * 
 */
public class GridWmsSource extends
        DefaultWmsSource<GridDimension, GridParamLayer, GridRecord> {

    protected ColormapStyleProvider<GridRecord> styler = new GridStyleProvider(
            this, "Grid/Default");

    public GridWmsSource(PluginProperties props,
            LayerTransformer<GridDimension, GridParamLayer> transformer)
            throws PluginException {
        super(props, props.getPluginName(), transformer);
    }

    @Override
    protected GridRecord getRecord(String layer, String time, String elevation,
            Map<String, String> dimensions) throws WmsException {
        LayerTransformer<GridDimension, GridParamLayer> transformer;
        List<GridRecord> res;
        try {
            transformer = getTransformer();
            res = GridRecordFinder.findWms(transformer, key, layer, time,
                    dimensions);
        } catch (OgcException e) {
            WmsException err = new WmsException(e);
            if (err.getCode().equals(Code.InternalServerError)) {
                log.error("Problem getting grib layer: " + layer);
            }
            throw err;
        }
        if (res.isEmpty()) {
            throw new WmsException(Code.LayerNotDefined,
                    "No layer matching all specified dimensions found");
        }
        if (res.size() > 1) {
            Collections.sort(res, new GridRecordFinder.Comp());
        }
        return res.get(0);
    }

    @Override
    protected ICoverageStyleProvider<GridRecord> getStyleProvider(String layer)
            throws WmsException {
        return styler;
    }

    @Override
    protected DatabaseQuery getRecordQuery(String layerName)
            throws OgcException {
        /* TODO merge this with GridRecordFinder */
        GridParamLayer layer = GridRecordFinder
                .getLayer(transformer, layerName);
        String parameter = layer.getParameter();
        parameter = GridRecordFinder.ogcToDbParameter(parameter);
        DatabaseQuery rval = new DatabaseQuery(GridRecord.class);
        rval.addQueryParam("info.parameter.abbreviation", parameter);
        return rval;
    }

}
