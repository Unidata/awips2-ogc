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
package com.raytheon.uf.edex.wcs.reg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a coverage range field from the OGC Web Coverage Service
 * specification
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 3, 2013            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class RangeField {

    protected static Pattern fieldPattern = Pattern
            .compile("^([^\\[:]+)(:([^\\[]+))?(\\[(.*)\\])?$");

    public enum InterpolationType {
        nearest, linear, cubic, quadratic, none
    };

    protected String identifier;

    protected RangeFieldDefinition definition;

    protected InterpolationType defaultInterpolation = InterpolationType.none;

    protected List<InterpolationType> additionalInterpolations;

    protected List<RangeAxis> axis;

    protected String nullValue;

    /**
     * @param identifier
     * @param description
     * @param definition
     */
    public RangeField(String identifier, RangeFieldDefinition definition) {
        super();
        this.identifier = identifier;
        this.definition = definition;
    }

    /**
     * @param rangeSubset
     *            string that conforms to the following grammar<br/>
     *            RangeSubset = FieldSubset *( “;” FieldSubset ) <br/>
     *            FieldSubset = FieldId [ “:” Interpolation ] [ “[” AxisSubsets
     *            “]” ]<br/>
     *            AxisSubsets = AxisSubset *( “,” AxisSubset ) <br/>
     *            AxisSubset = AxisId “[” Keys “]” <br/>
     *            Keys = Key *( “,” Key )
     * @return
     * @throws RangeParseException
     *             if argument doesn't match above grammar or interpolation type
     *             isn't recognized
     */
    public static List<RangeField> getRanges(String rangeSubset)
            throws RangeParseException {
        if (rangeSubset == null || rangeSubset.isEmpty()) {
            throw new RangeParseException(
                    "range subset cannot be null or empty");
        }
        String[] parts = rangeSubset.split(";");
        ArrayList<RangeField> rval = new ArrayList<RangeField>(parts.length);
        for (String s : parts) {
            rval.add(getField(s));
        }
        return rval;
    }

    /**
     * @param fieldSubset
     *            string that conforms to the following grammar<br/>
     *            FieldSubset = FieldId [ “:” Interpolation ] [ “[” AxisSubsets
     *            “]” ]<br/>
     *            AxisSubsets = AxisSubset *( “,” AxisSubset ) <br/>
     *            AxisSubset = AxisId “[” Keys “]” <br/>
     *            Keys = Key *( “,” Key )
     * @return
     * @throws RangeParseException
     *             if argument doesn't match above grammar or interpolation type
     *             isn't recognized
     */
    public static RangeField getField(String fieldSubset)
            throws RangeParseException {
        if (fieldSubset == null) {
            throw new RangeParseException("field subset cannot be null");
        }
        Matcher m = fieldPattern.matcher(fieldSubset);
        if (m.matches()) {
            String id = m.group(1);
            String interp = m.group(3);
            RangeField rval = new RangeField(id, null);
            rval.setDefaultInterpolation(getInterp(interp));
            if (m.group(5) != null) {
                rval.setAxis(RangeAxis.getAxisList(m.group(5)));
            }
            return rval;
        }
        throw new RangeParseException("Invalid field subset: " + fieldSubset);
    }

    /**
     * @param interp
     * @return
     * @throws RangeParseException
     */
    protected static InterpolationType getInterp(String interp)
            throws RangeParseException {
        InterpolationType rval = InterpolationType.none;
        try {
            if (interp != null) {
                rval = InterpolationType.valueOf(interp);
            }
        } catch (Throwable t) {
            throw new RangeParseException("Invalid interpolation type: "
                    + interp, t);
        }
        return rval;
    }

    @Override
    public String toString() {
        String rval = String.valueOf(identifier);
        if (defaultInterpolation != null
                && !defaultInterpolation.equals(InterpolationType.none)) {
            rval = rval + ':' + defaultInterpolation;
        }
        if (axis != null && !axis.isEmpty()) {
            List<String> strList = new ArrayList<String>(axis.size());
            for (RangeAxis ra : axis) {
                strList.add(ra.toString());
            }
            String axisStr = StringUtils.join(strList, ',');
            rval = rval + '[' + axisStr + ']';
        }
        return rval;
    }

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public RangeFieldDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(RangeFieldDefinition definition) {
        this.definition = definition;
    }

    public InterpolationType getDefaultInterpolation() {
        return defaultInterpolation;
    }

    public void setDefaultInterpolation(InterpolationType defaultInterpolation) {
        this.defaultInterpolation = defaultInterpolation;
    }

    public List<InterpolationType> getAdditionalInterpolations() {
        return additionalInterpolations;
    }

    public void setAdditionalInterpolations(
            List<InterpolationType> additionalInterpolations) {
        this.additionalInterpolations = additionalInterpolations;
    }

    public List<RangeAxis> getAxis() {
        return axis;
    }

    public void setAxis(List<RangeAxis> axis) {
        this.axis = axis;
    }

}
