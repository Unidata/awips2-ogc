/**
 * Copyright 09/24/12 Raytheon Company.
 *
 * Unlimited Rights
 * This software was developed pursuant to Contract Number 
 * DTFAWA-10-D-00028 with the US Government. The US Government’s rights 
 * in and to this copyrighted software are as specified in DFARS
 * 252.227-7014 which was made part of the above contract. 
 */
package com.raytheon.uf.edex.plugin.grid.ogc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.edex.ogc.common.db.SimpleLayer;

/**
 * Grid layer that sorts dimensions by parameter
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2013            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class GridCompositeLayer extends GridLayer {

    private static final long serialVersionUID = 6178813246077412635L;

    @XmlElement
    @DynamicSerializeElement
    protected HashMap<String, TreeSet<GridDimension>> dimensions = new HashMap<String, TreeSet<GridDimension>>();

    @XmlElement
    @DynamicSerializeElement
    protected HashMap<String, TreeSet<Date>> timeMap = new HashMap<String, TreeSet<Date>>();

    /**
     * 
     */
    public GridCompositeLayer() {
        super();
    }

    /**
     * @param other
     */
    public GridCompositeLayer(GridCompositeLayer other) {
        super(other);
        this.dimensions = new HashMap<String, TreeSet<GridDimension>>(
                other.dimensions.size());
        for (Entry<String, TreeSet<GridDimension>> e : other.dimensions
                .entrySet()) {
            TreeSet<GridDimension> set = new TreeSet<GridDimension>();
            GridDimension.copy(set, e.getValue());
            this.dimensions.put(e.getKey(), set);
        }
        this.timeMap = new HashMap<String, TreeSet<Date>>(other.timeMap.size());
        for (Entry<String, TreeSet<Date>> e : other.timeMap.entrySet()) {
            this.timeMap.put(e.getKey(), new TreeSet<Date>(e.getValue()));
        }
    }

    @Override
    public Set<GridDimension> getDimensions() {
        HashMap<String, GridDimension> byDim = new HashMap<String, GridDimension>();
        for (Entry<String, TreeSet<GridDimension>> e : dimensions.entrySet()) {
            for (GridDimension dim : e.getValue()) {
                GridDimension aggregate = byDim.get(dim.getName());
                if (aggregate == null) {
                    byDim.put(dim.getName(), dim);
                    continue;
                }
                aggregate.getValues().addAll(dim.getValues());
            }
        }
        return new TreeSet<GridDimension>(byDim.values());
    }

    @Override
    public TreeSet<Date> getTimes() {
        TreeSet<Date> rval = new TreeSet<Date>();
        for (Entry<String, TreeSet<Date>> e : timeMap.entrySet()) {
            rval.addAll(e.getValue());
        }
        return rval;
    }

    /**
     * Add time for parameter
     * 
     * @param parameter
     * @param time
     */
    public void addTime(String parameter, Date time) {
        TreeSet<Date> set = timeMap.get(parameter);
        if (set == null) {
            set = new TreeSet<Date>();
            timeMap.put(parameter, set);
        }
        set.add(time);
    }

    /**
     * @param parameter
     * @return empty list if parameter has no times
     */
    public TreeSet<Date> getTimes(String parameter) {
        TreeSet<Date> treeSet = timeMap.get(parameter);
        if (treeSet == null) {
            return new TreeSet<Date>();
        } else {
            return new TreeSet<Date>(treeSet);
        }
    }

    @Override
    public void updateDates(SimpleLayer<GridDimension> other) {
        if (!(other instanceof GridCompositeLayer)) {
            return;
        }
        GridCompositeLayer shiny = (GridCompositeLayer) other;
        for (Entry<String, TreeSet<Date>> e : shiny.timeMap.entrySet()) {
            TreeSet<Date> thisSet = this.timeMap.get(e.getKey());
            if (thisSet == null) {
                this.timeMap.put(e.getKey(), new TreeSet<Date>(e.getValue()));
            } else {
                thisSet.addAll(e.getValue());
            }
        }
    }

    /**
     * @param parameter
     * @return empty set if no dimensions for parameter
     */
    public TreeSet<GridDimension> getDimensions(String parameter) {
        TreeSet<GridDimension> rval = dimensions.get(parameter);
        if (rval == null) {
            return new TreeSet<GridDimension>();
        }
        return rval;
    }

    /**
     * @param parameter
     * @param dims
     */
    public void addDimensions(String parameter, TreeSet<GridDimension> dims) {
        dimensions.put(parameter, dims);
    }

    @Override
    public void updateDims(SimpleLayer<GridDimension> other) {
        if (!(other instanceof GridCompositeLayer)) {
            return;
        }
        GridCompositeLayer shiny = (GridCompositeLayer) other;
        for (String key : shiny.dimensions.keySet()) {
            TreeSet<GridDimension> otherDims = shiny.dimensions.get(key);
            TreeSet<GridDimension> thisDims = dimensions.get(key);
            if (thisDims == null) {
                thisDims = new TreeSet<GridDimension>();
                GridDimension.copy(thisDims, otherDims);
                dimensions.put(key, thisDims);
            } else {
                updateDimLists(thisDims, otherDims);
            }
        }
    }

    @Override
    public GridDimension getDimension(String dimension) {
        // TODO slow
        return getDimMap(getDimensions()).get(dimension);
    }

    /**
     * @return set of parameter names for composite layer
     */
    public Set<String> getParameters() {
        return dimensions.keySet();
    }

}
