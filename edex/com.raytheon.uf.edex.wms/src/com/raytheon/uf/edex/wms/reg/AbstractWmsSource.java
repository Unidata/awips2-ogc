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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.PluginProperties;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;
import com.raytheon.uf.edex.database.plugin.PluginDao;
import com.raytheon.uf.edex.database.plugin.PluginFactory;
import com.raytheon.uf.edex.database.query.DatabaseQuery;
import com.raytheon.uf.edex.ogc.common.IStyleLookupCallback;
import com.raytheon.uf.edex.ogc.common.OgcException;
import com.raytheon.uf.edex.ogc.common.OgcLayer;
import com.raytheon.uf.edex.ogc.common.OgcStyle;
import com.raytheon.uf.edex.ogc.common.StyleLookup;
import com.raytheon.uf.edex.ogc.common.db.ILayerCache;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer;
import com.raytheon.uf.edex.ogc.common.db.LayerTransformer.TimeFormat;
import com.raytheon.uf.edex.ogc.common.db.SimpleDimension;
import com.raytheon.uf.edex.ogc.common.db.SimpleLayer;
import com.raytheon.uf.edex.wms.WmsException;
import com.raytheon.uf.edex.wms.WmsException.Code;

/**
 * Base OGC Web Map Service plugin implementation
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
public abstract class AbstractWmsSource<D extends SimpleDimension, L extends SimpleLayer<D>, R extends PluginDataObject>
        implements WmsSource, IStyleLookupCallback<R> {

    protected static final String TIME_FIELD = "datatime";

    private PluginDao _dao;

    protected PluginProperties props;

    protected String key;

    protected String layerTable;

    protected LayerTransformer<D, L> transformer;

    protected TimeFormat timeFormat = TimeFormat.LIST;

    protected IUFStatusHandler log = UFStatus.getHandler(this.getClass());

    protected boolean layerTableIsWrapped = false;

    protected boolean wmtsCapable = true;

    public AbstractWmsSource(PluginProperties props, String key,
            LayerTransformer<D, L> transformer) {
        this.props = props;
        this.key = key;
        this.transformer = transformer;
    }

    public abstract Collection<OgcStyle> getStyles();

    protected PluginDao getDao() throws PluginException {
        if (_dao == null) {
            _dao = PluginFactory.getInstance().getPluginDao(
                    props.getPluginName());
        }
        return _dao;
    }

    protected LayerTransformer<D, L> getTransformer() throws WmsException {
        return transformer;
    }

    protected abstract StyleLookup getStyleLookup() throws WmsException;

    @Override
    public Collection<OgcLayer> listLayers() {
        try {
            LayerTransformer<?, ?> transformer = getTransformer();
            OgcLayer rval = new OgcLayer();
            rval.setTitle(transformer.getKey());
            StyleLookup lookup = getStyleLookup();
            rval.setChildren(transformer.getLayersAsOgc(timeFormat, lookup));
            rval.setStyles(getStyles());
            return Arrays.asList(rval);
        } catch (Exception e) {
            log.error("Unable to layers", e);
            return new ArrayList<OgcLayer>(0);
        }
    }

    @Override
    public OgcLayer getLayer(String layerName) throws WmsException {
        try {
            LayerTransformer<D, L> transformer = getTransformer();
            String[] parts = OgcLayer.separateKey(layerName);
            L layer = transformer.find(parts[1]);
            if (layer == null) {
                return null;
            }
            StyleLookup lookup = getStyleLookup();
            return transformer.transform(layer, timeFormat, lookup);
        } catch (Exception e) {
            log.error("Problem querying for layer", e);
            throw new WmsException(Code.InternalServerError);
        }
    }

    protected static Date stringToDate(String time) throws WmsException {
        try {
            return DatatypeConverter.parseDateTime(time).getTime();
        } catch (Exception e) {
            throw new WmsException(Code.InvalidFormat, "Invalid Date Format");
        }
    }

    protected Date parseTimeInstance(String time, String layer)
            throws WmsException {
        Date[] times = parseTimeString(time);
        if (times.length > 1) {
            // ranges not supported
            String lname = OgcLayer.createName(key, layer);
            String msg = String.format(
                    "Layer '%s' does not support time ranges", lname);
            throw new WmsException(Code.InvalidParameterValue, msg);
        }
        return times[0];
    }

    /**
     * Get the record associated with the given layer name, time, and if
     * applicable elevation and dimensions.
     * <p>
     * More elaborate datatypes will probably need to override this method to
     * return the correct pdo for the given arguments. The default
     * implementation simply builds a dataURI based off the given layer and
     * time.
     * 
     * @param layer
     * @param time
     * @param elevation
     * @param dimensions
     * @return
     * @throws WmsException
     */
    @SuppressWarnings("unchecked")
    protected R getRecord(String layer, String time, String elevation,
            Map<String, String> dimensions) throws WmsException {
        Date targetDate;
        if (time == null) {
            targetDate = getDefaultDate(layer);
        } else {
            targetDate = parseTimeInstance(time, layer);
        }
        R rval = null;
        try {
            PluginDao dao = getDao();
            DatabaseQuery query = getQuery(layer, targetDate, elevation);
            List<R> results = (List<R>) dao.queryByCriteria(query);
            if (!results.isEmpty()) {
                rval = results.get(0);
            }
        } catch (PluginException | DataAccessLayerException e) {
            log.error("Unable to query metdata", e);
            throw new WmsException(Code.InternalServerError);
        }
        if (rval == null) {
            throw new WmsException(Code.LayerNotDefined);
        }
        return rval;
    }

    protected DatabaseQuery getQuery(String layer, Date time, String elevation)
            throws WmsException {
        DatabaseQuery rval;
        try {
            rval = getRecordQuery(layer);
        } catch (OgcException e) {
            throw new WmsException(e);
        }
        rval.addQueryParam(TIME_FIELD, new DataTime(time));
        return rval;
    }

    /**
     * Get database query that retrieves all records for layer
     * 
     * @param layer
     * @return
     * @throws OgcException
     */
    protected abstract DatabaseQuery getRecordQuery(String layer)
            throws OgcException;

    /**
     * @param time
     * @return array with one entry if instance. If string is time range,
     *         returned array will have range start at index 0 and range end at
     *         index 1
     * @throws WmsException
     */
    protected Date[] parseTimeString(String time) throws WmsException {
        String[] parts = StringUtils.split(time, '/');
        Date[] rval;
        try {
            if (parts.length == 1) {
                // instance
                rval = new Date[] { stringToDate(parts[0]) };
            } else {
                // range
                Date start = stringToDate(parts[0]);
                Date end = stringToDate(parts[1]);
                // TODO check resolution
                rval = new Date[] { start, end };
            }
        } catch (IllegalArgumentException e) {
            // assume malformed time
            throw new WmsException(Code.InvalidParameterValue,
                    "Invalid time string: " + time);
        }
        return rval;
    }

    protected String parseIncomingLayerName(String rawLayer) {
        return OgcLayer.separateKey(rawLayer)[1];
    }

    /**
     * Get the URI for the given layer and time.
     * 
     * @param layer
     *            the layer to get.
     * @param time
     *            the time to get.
     * @return the dataURI for the given layer and time.
     */
    protected String buildURI(String layer, Date time) {
        /*
         * when the tstamp is written to the db it has an _ separating the date
         * and time
         */
        String tstamp = TimeUtil.formatToSqlTimestamp(time).replace(' ', '_');
        return ("/" + key + "/" + tstamp + "/" + layer);
    }

    /**
     * Return the default date for specified layer name
     * 
     * @param layerName
     * @return
     * @throws WmsException
     */
    protected Date getDefaultDate(String layerName) throws WmsException {
        Date rval = null;
        try {
            LayerTransformer<D, L> transformer = getTransformer();
            ILayerCache<D, L> lcache = transformer.getLcache();
            rval = lcache.getLatestTime(layerName);
        } catch (OgcException e) {
            log.error("Unable to query layers", e);
            throw new WmsException(Code.InternalServerError);
        }
        if (rval == null) {
            throw new WmsException(Code.LayerNotDefined);
        }
        return rval;
    }

    @Override
    public R lookupSample(String layerName) throws OgcException {
        try {
            return getRecord(layerName, null, null,
                    new HashMap<String, String>(0));
        } catch (WmsException e) {
            throw new OgcException(OgcException.Code.InternalServerError, e);
        }
    }

    @Override
    public List<R> getAllSamples() throws OgcException {
        List<L> layers = transformer.getLayers();
        List<R> rval = new ArrayList<R>(layers.size());
        for (L layer : layers) {
            rval.add(lookupSample(layer.getName()));
        }
        return rval;
    }

    @Override
    public boolean hasUpdated() {
        return true;
    }

    @Override
    public String getKey() {
        return key;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public boolean isLayerTableIsWrapped() {
        return layerTableIsWrapped;
    }

    /**
     * @return the wmtsCapable
     */
    @Override
    public boolean isWmtsCapable() {
        return wmtsCapable;
    }

    /**
     * @param wmsCapable
     *            the wmsCapable to set
     */
    public void setWmtsCapable(boolean wmtsCapable) {
        this.wmtsCapable = wmtsCapable;
    }

}
