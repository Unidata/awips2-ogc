/**
 * Copyright 09/24/12 Raytheon Company.
 *
 * Unlimited Rights
 * This software was developed pursuant to Contract Number 
 * DTFAWA-10-D-00028 with the US Government. The US Government’s rights 
 * in and to this copyrighted software are as specified in DFARS
 * 252.227-7014 which was made part of the above contract. 
 */
package com.raytheon.uf.edex.wcs.reg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a three dimensional cube of data at a specific point in time
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 2, 2013            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class TemporalCube implements Comparable<TemporalCube> {

    private final List<VerticalSlice> slices;

    private final Date time;

    private final CoverageZAxis zAxis;

    /**
     * 
     */
    public TemporalCube(Date time, CoverageZAxis zAxis) {
        this(new ArrayList<VerticalSlice>(), time, zAxis);
    }

    /**
     * @param cube
     */
    public TemporalCube(List<VerticalSlice> slices, Date time,
            CoverageZAxis zAxis) {
        this.slices = slices;
        this.time = time;
        this.zAxis = zAxis;
    }

    /**
     * @return the cube
     */
    public List<VerticalSlice> getSlices() {
        return slices;
    }

    public void add(VerticalSlice slice) {
        this.slices.add(slice);
    }

    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TemporalCube other = (TemporalCube) obj;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        return true;
    }

    @Override
    public int compareTo(TemporalCube o) {
        if (o == null) {
            return 1;
        }
        return this.time.compareTo(o.time);
    }

    /**
     * @return the zAxis
     */
    public CoverageZAxis getzAxis() {
        return zAxis;
    }

}
