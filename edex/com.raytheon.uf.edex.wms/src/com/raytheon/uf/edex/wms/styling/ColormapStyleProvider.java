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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import org.apache.commons.collections.map.LRUMap;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;

import com.raytheon.uf.common.colormap.ColorMap;
import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.ColorMapLoader;
import com.raytheon.uf.common.colormap.IColorMap;
import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.image.Colormapper;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.style.ParamLevelMatchCriteria;
import com.raytheon.uf.common.style.StyleException;
import com.raytheon.uf.common.style.StyleManager;
import com.raytheon.uf.common.style.StyleRule;
import com.raytheon.uf.common.style.image.ColorMapParameterFactory;
import com.raytheon.uf.common.style.image.ImagePreferences;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import com.raytheon.uf.edex.database.plugin.PluginFactory;
import com.raytheon.uf.edex.ogc.common.IStyleLookupCallback;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.reprojection.ReferencedDataRecord;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;
import com.raytheon.uf.edex.wms.reg.WmsImage;
import com.raytheon.uf.edex.wms.util.ColorMapUtility;
import com.raytheon.uf.edex.wms.util.LegendUtility;

/**
 * Abstract class for styling data using an AWIPS II colormap
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
public abstract class ColormapStyleProvider<R extends PluginDataObject>
        implements ICoverageStyleProvider<R> {

    @SuppressWarnings("unchecked")
    protected static final Map<String, ColorMapParameters> PARAM_CACHE = new LRUMap(
            32);

    protected String styleLibraryFileName;

    protected static IUFStatusHandler log = UFStatus
            .getHandler(ColormapStyleProvider.class);

    protected String fallbackDefaultColormapName = "Default";

    protected Style preRendered;

    public static int defaultWidth = 512;

    public static int defaultHeight = 30;

    protected ClassLoader loader = null;

    protected IStyleLookupCallback<R> callback;

    public ColormapStyleProvider(IStyleLookupCallback<R> callback,
            String defaultColormap) {
        this(callback);
        fallbackDefaultColormapName = defaultColormap;
    }

    public ColormapStyleProvider(IStyleLookupCallback<R> callback) {
        this.callback = callback;
        StyleBuilder sb = new StyleBuilder();
        RasterSymbolizer symbolizer = sb.createRasterSymbolizer();
        symbolizer.setOpacity(new FilterFactoryImpl()
                .createLiteralExpression(1.0));
        preRendered = sb.createStyle(symbolizer);
    }

    /**
     * Create colormap parameters for data record
     * 
     * @param record
     * @return
     * @throws StyleException
     * @throws ColorTableException
     * @throws WmsException
     */
    protected ColorMapParameters getCmapParams(R record) throws WmsException {
        ColorMapParameters rval;
        synchronized (PARAM_CACHE) {
            rval = PARAM_CACHE.get(record.getDataURI());
        }
        if (rval == null) {
            Object rawData = getRawData(record);
            return getCmapParams(record, rawData);
        }
        return rval;
    }

    /**
     * Create colormap parameters for data record
     * 
     * @param record
     * @param rawData
     * @return
     * @throws StyleException
     * @throws ColorTableException
     * @throws WmsException
     */
    protected ColorMapParameters getCmapParams(R record, Object rawData)
            throws WmsException {
        ColorMapParameters rval;
        synchronized (PARAM_CACHE) {
            rval = PARAM_CACHE.get(record.getDataURI());
        }
        if (rval == null) {
            Unit<?> paramUnits = getParamUnits(record);
            ParamLevelMatchCriteria criteria = getCriteria(record);
            try {
                rval = ColorMapParameterFactory.build(rawData, paramUnits,
                        criteria);
                String colorMapName = rval.getColorMapName();
                if (colorMapName == null) {
                    colorMapName = getFallbackDefaultColormapName();
                    rval.setColorMapName(colorMapName);
                }
                IColorMap cmap = ColorMapLoader.loadColorMap(colorMapName);
                rval.setColorMap(cmap);
                rval = finalizeCmapParams(record, rval);
                synchronized (PARAM_CACHE) {
                    PARAM_CACHE.put(record.getDataURI(), rval);
                }
            } catch (Exception e) {
                log.error("Unable to create colormap params for record: "
                        + record, e);
                throw new WmsException(Code.InternalServerError);
            }
        }
        return rval;
    }

    /**
     * Hook to modify cmap parameters
     * 
     * @param record
     * @param params
     * @return
     */
    protected ColorMapParameters finalizeCmapParams(R record,
            ColorMapParameters params) {
        return params;
    }

    protected abstract ParamLevelMatchCriteria getCriteria(R record)
            throws WmsException;

    protected abstract Unit<?> getParamUnits(R record) throws WmsException;

    protected Object getRawData(R record) throws WmsException {
        Object data;
        try {
            PluginDao dao = PluginFactory.getInstance().getPluginDao(
                    record.getPluginName());
            IDataRecord[] res = dao.getHDF5Data(record, 0);
            IDataRecord datarecord = res[0];
            data = datarecord.getDataObject();
        } catch (PluginException e) {
            log.error("Unable to retrieve data for record: " + record, e);
            throw new WmsException(Code.InternalServerError);
        }
        return data;
    }

    @Override
    public String lookup(String layername) {
        try {
            R sample = callback.lookupSample(layername);
            return lookupInternal(sample);
        } catch (Exception e) {
            log.error("Unable to lookup sample record for layer: " + layername,
                    e);
            return null;
        }
    }

    private String lookupInternal(R sample) throws WmsException {
        ParamLevelMatchCriteria criteria = getCriteria(sample);
        try {
            StyleRule sr = StyleManager.getInstance().getStyleRule(
                    StyleManager.StyleType.IMAGERY, criteria);
            if (sr == null) {
                return getFallbackDefaultColormapName();
            }
            ImagePreferences prefs = (ImagePreferences) sr.getPreferences();
            String rval = prefs.getDefaultColormap();
            if (rval == null) {
                rval = getFallbackDefaultColormapName();
            }
            return rval;
        } catch (StyleException e) {
            log.error("Unable to get style for record: " + sample, e);
            throw new WmsException(Code.InternalServerError, e);
        }
    }

    @Override
    public WmsImage styleData(IWmsDataRetriever retriever,
            WmsStyleChoice styleChoice, R record, GridGeometry2D geom)
            throws WmsException {
        WmsImage rval;
        ReferencedEnvelope env = new ReferencedEnvelope(geom.getEnvelope2D(),
                geom.getCoordinateReferenceSystem());
        ReferencedDataRecord dataRecord = retriever.getDataRecord(record, env);
        if (dataRecord == null) {
            rval = new WmsImage((GridCoverage2D) null);
        } else {
            try {
                ColorMapParameters cmapParams = styleChoice.getCmapParams();
                if (cmapParams == null) {
                    cmapParams = getCmapParams(record);
                }
                return styleData(dataRecord, cmapParams);
            } catch (Exception e) {
                log.error("Problem applying colormap", e);
                throw new WmsException(Code.InternalServerError);
            }
        }
        return rval;
    }

    /**
     * Color a ReferencedDataRecord with the given ColorMapParameters
     * 
     * @param data
     * @param record
     * @param cmapParams
     * @return
     * @throws WmsException
     */
    protected WmsImage styleData(ReferencedDataRecord data,
            ColorMapParameters cmapParams) throws WmsException {
        WmsImage rval;
        if (data == null) {
            rval = new WmsImage((GridCoverage2D) null);
        } else {
            try {
                ColorMapData cmapData = ColorMapUtility.buildColorMapData(data
                        .getRecord());
                RenderedImage image = Colormapper
                        .colorMap(cmapData, cmapParams);
                GeneralEnvelope ge = new GeneralEnvelope(2);
                ReferencedEnvelope re = data.getEnvelope();
                ge.setCoordinateReferenceSystem(re
                        .getCoordinateReferenceSystem());
                ge.setRange(0, re.getMinX(), re.getMaxX());
                ge.setRange(1, re.getMinY(), re.getMaxY());
                GridCoverage2D gc = convert(image, ge);
                return new WmsImage(gc, preRendered);

            } catch (Exception e) {
                log.error("Problem applying colormap", e);
                throw new WmsException(Code.InternalServerError);
            }
        }
        return rval;
    }

    public WmsImage justStyleRecord(ReferencedDataRecord dataRecord,
            R pluginRecord) throws WmsException {
        ColorMapParameters cmapParams = getCmapParams(pluginRecord, dataRecord
                .getRecord().getDataObject());
        return styleData(dataRecord, cmapParams);
    }

    protected GridCoverage2D convert(RenderedImage img, GeneralEnvelope bounds) {
        GridCoverageFactory fact = new GridCoverageFactory();
        return fact.create("", img, bounds);
    }

    @Override
    public WmsStyleChoice getStyle(String layer, R record, String style)
            throws WmsException {
        ColorMapParameters params = getCmapParams(record);
        IColorMap cmap;
        if (style != null && !style.isEmpty()) {
            cmap = getJustColormap(style);
            if (cmap == null) {
                throw new WmsException(Code.StyleNotDefined, "Unknown style: "
                        + style);
            }
            params.setColorMapName(style);
            params.setColorMap(cmap);
        }
        return new WmsStyleChoice(params);
    }

    /**
     * @param style
     * @return null if colormap is not found
     * @throws WmsException
     */
    protected IColorMap getJustColormap(String style) throws WmsException {
        try {
            return ColorMapLoader.loadColorMap(style);
        } catch (ColorMapException e) {
            log.error("could not load colormap " + style, e);
            // error is likely that the style is not found, return null
            return null;
        }
    }

    /**
     * the fallback default style, when all else fails
     * 
     * @return
     */
    protected String getFallbackDefaultColormapName() {
        return fallbackDefaultColormapName;
    }

    @Override
    public List<OgcStyle> getStyles() {
        try {
            List<R> samples = callback.getAllSamples();
            List<OgcStyle> rval = new ArrayList<OgcStyle>(samples.size());
            for (R record : samples) {
                rval.add(new OgcStyle(lookupInternal(record)));
            }
            return rval;
        } catch (Exception e) {
            log.error("Unable to lookup styles", e);
            return new ArrayList<OgcStyle>(0);
        }
    }

    @Override
    public BufferedImage getLegend(String layer, R record, String style,
            Integer width, Integer height) throws WmsException {
        if (width == null || height == null) {
            width = defaultWidth;
            height = defaultHeight;
        }
        try {
            ColorMapParameters cmapParams = getCmapParams(record);
            float[] values = getLabelValues(cmapParams);
            float dataMin = cmapParams.getDataMin();
            float dataMax = cmapParams.getDataMax();
            UnitConverter converter = cmapParams.getDataToDisplayConverter();
            if (converter != null) {
                dataMin = (float) converter.convert(dataMin);
                dataMax = (float) converter.convert(dataMax);
            }
            BufferedImage labels = LegendUtility.buildLabels(width, height,
                    values, dataMin, dataMax);
            ColorMap cmap = (ColorMap) cmapParams.getColorMap();
            return getLegendBar(cmap, cmapParams, labels, width, height);
        } catch (Exception e) {
            log.error("Problem applying colormap", e);
            throw new WmsException(Code.InternalServerError);
        }
    }

    private float[] getLabelValues(ColorMapParameters cmapParams) {
        return cmapParams.getColorBarIntervals();
    }

    /**
     * Draw the legend background
     * 
     * @param cmap
     * @param params
     * @param labels
     *            labels to be drawn on the foreground, can be null
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage getLegendBar(ColorMap cmap,
            ColorMapParameters params, BufferedImage labels, Integer width,
            Integer height) {

        BufferedImage legend = null;
        // Create the color bar.
        BufferedImage colorBar = LegendUtility.buildColorbar(cmap, width,
                height);
        if (colorBar != null) {
            legend = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics g = legend.getGraphics();
            g.drawImage(colorBar, 0, 0, null);
            if (labels != null) {
                g.drawImage(labels, 0, 0, null);
            }
        }

        return legend;
    }

    @Override
    public double convertToDisplay(String layer, R record, double value)
            throws WmsException {
        ColorMapParameters cmapParams = getCmapParams(record);
        UnitConverter converter = cmapParams.getDataToDisplayConverter();
        if (converter == null) {
            return value;
        }
        return converter.convert(value);
    }

    /**
     * @return the loader
     */
    public ClassLoader getLoader() {
        if (loader == null) {
            return this.getClass().getClassLoader();
        }
        return loader;
    }

    /**
     * @param loader
     *            the loader to set
     */
    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    /**
     * @param styleLibraryFileName
     *            the styleLibraryFileName to set
     */
    public void setStyleLibraryFileName(String styleLibraryFileName) {
        this.styleLibraryFileName = styleLibraryFileName;
    }

    /**
     * @param fallbackDefaultColormapName
     *            the fallbackDefaultColormapName to set
     */
    public void setFallbackDefaultColormapName(
            String fallbackDefaultColormapName) {
        this.fallbackDefaultColormapName = fallbackDefaultColormapName;
    }

}
