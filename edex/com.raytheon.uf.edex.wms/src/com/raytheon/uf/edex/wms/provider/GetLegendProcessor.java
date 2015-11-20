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
package com.raytheon.uf.edex.wms.provider;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import org.geotools.styling.StyledLayerDescriptor;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.reg.WmsSource;
import com.raytheon.uf.edex.wms.styling.SldStyleProvider;
import com.raytheon.uf.edex.wms.util.StyleLibrary;

/**
 * Handles get requests for map legend graphics
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
public class GetLegendProcessor {

	protected String time;

	protected String elevation;

	protected Map<String, String> dimensions;

	protected Integer width;

	protected Integer height;

	protected String username;

	protected Set<String> roles;

	protected WmsLayerManager layerManager;

    protected IUFStatusHandler log = UFStatus.getHandler(this.getClass());

	public GetLegendProcessor(WmsLayerManager layerManager, String time,
			String elevation, Map<String, String> dimensions, Integer width,
			Integer height, String username, String[] roles) {
		super();
		this.layerManager = layerManager;
		this.time = time;
		this.elevation = elevation;
		this.dimensions = dimensions;
		this.width = width;
		this.height = height;
	}

	protected WmsSource getSource(String layer) throws WmsException {
		return layerManager.getSource(layer, username, roles);
	}

	public BufferedImage getLegend(String layerName, String styleName,
			boolean includeLabels) throws WmsException {
		WmsSource source = getSource(layerName);
		return source.getLegend(layerName, styleName, time, elevation,
				dimensions, height, width);
	}

	public BufferedImage getLegendSld(StyledLayerDescriptor sld)
			throws WmsException {
		StyleLibrary lib = new StyleLibrary(sld);
		return SldStyleProvider.getLegend(lib.getAny(), width, height);
	}

	/**
	 * @param layer
	 * @param style
	 * @param sld
	 * @return
	 * @throws WmsException
	 */
    public BufferedImage getLegendStyleLib(String layer, String datauri,
            String style, StyledLayerDescriptor sld) throws WmsException {
		SldStyleProvider styler = new SldStyleProvider(sld);
        return styler.getLegend(layer, null, style, width, height);
	}

	public static BufferedImage applyBackground(BufferedImage img, Color bgColor) {
		BufferedImage rval = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = rval.createGraphics();
		g.setColor(bgColor);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return rval;
	}

}
