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
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Grid layer that has dimensions for a single parameter
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
public class GridParamLayer extends GridLayer {

    private static final long serialVersionUID = -5614814725742630980L;

    @XmlElement
    @DynamicSerializeElement
    protected TreeSet<GridDimension> dimensions;

    @XmlElement
    @DynamicSerializeElement
    protected String parameter;

    /**
     * 
     */
    public GridParamLayer() {
        super();
        dimensions = new TreeSet<GridDimension>();
    }

    /**
     * @param other
     */
    public GridParamLayer(GridParamLayer other) {
        super(other);
        this.parameter = other.parameter;
        this.dimensions = new TreeSet<GridDimension>();
        GridDimension.copy(this.dimensions, other.dimensions);
    }

    /**
     * @param other
     */
    public GridParamLayer(String parameter, GridCompositeLayer other) {
        super(other);
        this.parameter = parameter;
        this.times = new TreeSet<Date>(other.getTimes(parameter));
        TreeSet<GridDimension> otherDims = other.getDimensions(parameter);
        this.dimensions = new TreeSet<GridDimension>();
        for (GridDimension dim : otherDims) {
            if (dim.getName().equalsIgnoreCase(GridDimension.PARAM_DIM)) {
                continue;
            }
            this.dimensions.add(new GridDimension(dim));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.plugin.grib.ogc.GribLayer#getDimensions()
     */
    @Override
    public Set<GridDimension> getDimensions() {
        return dimensions;
    }

    /**
     * @return the parameter
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @param parameter
     *            the parameter to set
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * @param dimensions
     *            the dimensions to set
     */
    public void setDimensions(TreeSet<GridDimension> dimensions) {
        this.dimensions = dimensions;
    }

}