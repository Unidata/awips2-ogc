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
package com.raytheon.uf.edex.plugin.obs.ogc.metar;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataplugin.PluginProperties;
import com.raytheon.uf.common.dataplugin.obs.metar.MetarRecord;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.edex.database.query.DatabaseQuery;
import com.raytheon.uf.edex.ogc.common.OgcException;
import com.raytheon.uf.edex.ogc.common.db.DefaultPointDataDimension;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.reg.PointDataWmsSource;
import com.raytheon.uf.edex.wms.styling.FeatureStyleProvider;

/**
 * Provides Web Map Service access to Metar data
 * 
 * <pre>
 * 
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
public class MetarWmsSource extends
        PointDataWmsSource<DefaultPointDataDimension, MetarLayer> {

	private static final String geometryField = "location.location";

	private static final FeatureStyleProvider styler = new FeatureStyleProvider(
			"sld/metar/defaultMetar.sld");

	/**
	 * @param props
	 * @param key
	 * @param layerTable
	 * @param styles
	 * @throws Exception
	 */
    public MetarWmsSource(PluginProperties props,
            LayerTransformer<DefaultPointDataDimension, MetarLayer> transformer)
            throws Exception {
		super(props, "metar", transformer, new MetarFeatureFactory());
	}

	@Override
	protected String getGeometryField(String layer) {
		// metar has only one layer
		return geometryField;
	}

	@Override
	protected CoordinateReferenceSystem getCRS(String layer) {
		
		return MapUtil.LATLON_PROJECTION;
	}

	@Override
	protected FeatureStyleProvider getStyleProvider(String layer)
			throws WmsException {
		return styler;
	}

    @Override
    protected DatabaseQuery getRecordQuery(String layer) throws OgcException {
        return new DatabaseQuery(MetarRecord.class);
    }

}
