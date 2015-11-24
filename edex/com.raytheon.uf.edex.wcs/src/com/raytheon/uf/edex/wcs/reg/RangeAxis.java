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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a coverage range axis from the OGC Web Coverage Service
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
public class RangeAxis {

    protected String identifier;

    protected Set<String> keys;

    protected static Pattern axisPattern = Pattern
            .compile("^([^\\[]+)\\[([^\\]]+)]$");

    /**
     * @param description
     * @param identifier
     * @param keys
     */
    public RangeAxis(String identifier, Set<String> keys) {
        super();
        this.identifier = identifier;
        this.keys = keys;
    }

    /**
     * @param axisSubset
     *            string that conforms to the following grammar<br/>
     *            AxisSubsets = AxisSubset *( “,” AxisSubset ) <br/>
     *            AxisSubset = AxisId “[” Keys “]” <br/>
     *            Keys = Key *( “,” Key )
     * @return null if axisSubset is null
     * @throws RangeParseException
     *             if axisSubsets doesn't follow the above grammar
     */
    public static List<RangeAxis> getAxisList(String axisSubsets)
            throws RangeParseException {
        if (axisSubsets == null || axisSubsets.isEmpty()) {
            throw new RangeParseException(
                    "axis subsets cannot be null or empty");
        }
        List<String> parts = axisSplit(axisSubsets);
        List<RangeAxis> rval = new ArrayList<RangeAxis>(parts.size());
        for (String s : parts) {
            rval.add(getAxis(s));
        }
        return rval;
    }

    /**
     * @param axisSubset
     *            string that conforms to the following grammar<br/>
     *            AxisSubset = AxisId “[” Keys “]” <br/>
     *            Keys = Key *( “,” Key )
     * @return
     * @throws RangeParseException
     *             if axisSubset doesn't follow the above grammar
     */
    public static RangeAxis getAxis(String axisSubset)
            throws RangeParseException {
        if (axisSubset == null) {
            throw new RangeParseException("axis subset cannot be null");
        }
        Matcher m = axisPattern.matcher(axisSubset);
        if (m.matches()) {
            Set<String> keys = new HashSet<String>();
            keys.addAll(Arrays.asList(m.group(2).split(",")));
            return new RangeAxis(m.group(1), keys);
        }
        throw new RangeParseException("Invalid axis subset: " + axisSubset);
    }

    /**
     * @param axisStr
     * @return
     */
    protected static List<String> axisSplit(String axisStr) {
        int start = 0;
        int end = 0;
        int mark = -1;
        List<String> rval = new ArrayList<String>();
        do {
            mark = axisStr.indexOf(',', end);
            if (mark > 0 && axisStr.charAt(mark - 1) == ']') {
                rval.add(axisStr.substring(start, mark));
                start = mark + 1;
            }
            end = mark + 1;
        } while (mark > 0);
        rval.add(axisStr.substring(start));
        return rval;
    }

    @Override
    public String toString() {
        String rval = String.valueOf(identifier);
        if (keys != null && !keys.isEmpty()) {
            String keyStr = StringUtils.join(keys, ',');
            rval = rval + '[' + keyStr + ']';
        }
        return rval;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }

}
