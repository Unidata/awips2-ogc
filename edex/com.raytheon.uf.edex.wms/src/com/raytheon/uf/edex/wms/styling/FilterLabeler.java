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

import java.util.Iterator;
import java.util.List;

import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.BinaryTemporalOperator;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;

/**
 * Visitor pattern implementation to provide labels to filter objects
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
public class FilterLabeler implements FilterVisitor {

    protected boolean shortVersion = true;

    @Override
    public Object visitNullFilter(Object extraData) {
        return "null";
    }

    @Override
    public Object visit(ExcludeFilter filter, Object extraData) {
        return filter.toString();
    }

    @Override
    public Object visit(IncludeFilter filter, Object extraData) {
        return filter.toString();
    }

    @Override
    public Object visit(And filter, Object extraData) {
        return binaryLogic(filter, extraData, "and");
    }

    protected String binaryLogic(BinaryLogicOperator filter, Object extraData,
            String op) {
        List<Filter> children = filter.getChildren();
        if (children == null || children.isEmpty()) {
            return "";
        }
        Iterator<Filter> i = children.iterator();
        Filter next = i.next();
        if (!i.hasNext()) {
            return (String) next.accept(this, extraData);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(next.accept(this, extraData));
        while (i.hasNext()) {
            next = i.next();
            sb.append(" ").append(op).append(" ");
            sb.append(next.accept(this, extraData));
        }
        sb.append(" ");
        return sb.toString();
    }

    @Override
    public Object visit(Id filter, Object extraData) {
        return filter.toString();
    }

    @Override
    public Object visit(Not filter, Object extraData) {
        return "not " + filter.getFilter().accept(this, extraData);
    }

    @Override
    public Object visit(Or filter, Object extraData) {
        return binaryLogic(filter, extraData, "or");
    }

    @Override
    public Object visit(PropertyIsBetween filter, Object extraData) {
        return filter.getExpression();
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        return binaryComp(filter, extraData, shortVersion ? "=" : "equal to");
    }

    protected Object binaryComp(BinaryComparisonOperator filter,
            Object extraData, String op) {
        String left = filter.getExpression1().toString();
        String right = filter.getExpression2().toString();
        return left + " " + op + " " + right;
    }

    @Override
    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        return binaryComp(filter, extraData, shortVersion ? "!="
                : "not equal to");
    }

    @Override
    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        return binaryComp(filter, extraData, shortVersion ? ">"
                : "greater than");
    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        return binaryComp(filter, extraData, shortVersion ? ">="
                : "greater than or equal to");
    }

    @Override
    public Object visit(PropertyIsLessThan filter, Object extraData) {
        return binaryComp(filter, extraData, shortVersion ? "<" : "less than");
    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        return binaryComp(filter, extraData, shortVersion ? "<="
                : "less than or equal to");
    }

    @Override
    public Object visit(PropertyIsLike filter, Object extraData) {
        return filter.toString();
    }

    @Override
    public Object visit(PropertyIsNull filter, Object extraData) {
        String prop = filter.getExpression().toString();
        return prop + " is null";
    }

    @Override
    public Object visit(BBOX filter, Object extraData) {
        return binarySpatial(filter, extraData, "within");
    }

    protected Object binarySpatial(BinarySpatialOperator filter,
            Object extraData, String op) {
        String left = filter.getExpression1().toString();
        String right = filter.getExpression2().toString();
        return left + " " + op + " " + right;
    }

    protected Object binaryTemporal(BinaryTemporalOperator filter,
            Object extraData, String op) {
        String left = filter.getExpression1().toString();
        String right = filter.getExpression2().toString();
        return left + " " + op + " " + right;
    }

    @Override
    public Object visit(Beyond filter, Object extraData) {
        return binarySpatial(filter, extraData, "beyond");
    }

    @Override
    public Object visit(Contains filter, Object extraData) {
        return binarySpatial(filter, extraData, "contains");
    }

    @Override
    public Object visit(Crosses filter, Object extraData) {
        return binarySpatial(filter, extraData, "crosses");
    }

    @Override
    public Object visit(Disjoint filter, Object extraData) {
        return binarySpatial(filter, extraData, "disjoint");
    }

    @Override
    public Object visit(DWithin filter, Object extraData) {
        return binarySpatial(filter, extraData, "distance within");
    }

    @Override
    public Object visit(Equals filter, Object extraData) {
        return binarySpatial(filter, extraData, "equals");
    }

    @Override
    public Object visit(Intersects filter, Object extraData) {
        return binarySpatial(filter, extraData, "intersects");
    }

    @Override
    public Object visit(Overlaps filter, Object extraData) {
        return binarySpatial(filter, extraData, "overlaps");
    }

    @Override
    public Object visit(Touches filter, Object extraData) {
        return binarySpatial(filter, extraData, "touches");
    }

    @Override
    public Object visit(Within filter, Object extraData) {
        return binarySpatial(filter, extraData, "within");
    }

    /**
     * @return the shortVersion
     */
    public boolean isShortVersion() {
        return shortVersion;
    }

    /**
     * @param shortVersion
     *            the shortVersion to set
     */
    public void setShortVersion(boolean shortVersion) {
        this.shortVersion = shortVersion;
    }

    @Override
    public Object visit(PropertyIsNil filter, Object extraData) {
        String prop = filter.getExpression().toString();
        return prop + " is null";
    }

    @Override
    public Object visit(After after, Object extraData) {
        return binaryTemporal(after, extraData, "after");
    }

    @Override
    public Object visit(AnyInteracts anyInteracts, Object extraData) {
        return binaryTemporal(anyInteracts, extraData, "interacts");
    }

    @Override
    public Object visit(Before before, Object extraData) {
        return binaryTemporal(before, extraData, "before");
    }

    @Override
    public Object visit(Begins begins, Object extraData) {
        return binaryTemporal(begins, extraData, "begins");
    }

    @Override
    public Object visit(BegunBy begunBy, Object extraData) {
        return binaryTemporal(begunBy, extraData, "begun by");
    }

    @Override
    public Object visit(During during, Object extraData) {
        return binaryTemporal(during, extraData, "during");
    }

    @Override
    public Object visit(EndedBy endedBy, Object extraData) {
        return binaryTemporal(endedBy, extraData, "ended by");
    }

    @Override
    public Object visit(Ends ends, Object extraData) {
        return binaryTemporal(ends, extraData, "ends");
    }

    @Override
    public Object visit(Meets meets, Object extraData) {
        return binaryTemporal(meets, extraData, "meets");
    }

    @Override
    public Object visit(MetBy metBy, Object extraData) {
        return binaryTemporal(metBy, extraData, "met by");
    }

    @Override
    public Object visit(OverlappedBy overlappedBy, Object extraData) {
        return binaryTemporal(overlappedBy, extraData, "overlapped by");
    }

    @Override
    public Object visit(TContains contains, Object extraData) {
        return binaryTemporal(contains, extraData, "temporally contains");
    }

    @Override
    public Object visit(TEquals equals, Object extraData) {
        return binaryTemporal(equals, extraData, "temporally equals");
    }

    @Override
    public Object visit(TOverlaps contains, Object extraData) {
        return binaryTemporal(contains, extraData, "temporally overlaps");
    }

}
