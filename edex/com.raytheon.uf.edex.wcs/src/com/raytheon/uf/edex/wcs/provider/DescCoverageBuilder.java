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
package com.raytheon.uf.edex.wcs.provider;

import static com.raytheon.uf.edex.wcs.provider.WcsJaxbUtils.getAsLangString;
import static com.raytheon.uf.edex.wcs.provider.WcsJaxbUtils.getKeywords;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;

import net.opengis.gml.v_3_1_1.PolygonType;
import net.opengis.gml.v_3_1_1.TimePositionType;
import net.opengis.ows.v_1_1_0.AllowedValues;
import net.opengis.ows.v_1_1_0.AnyValue;
import net.opengis.ows.v_1_1_0.BoundingBoxType;
import net.opengis.ows.v_1_1_0.CodeType;
import net.opengis.ows.v_1_1_0.DomainMetadataType;
import net.opengis.ows.v_1_1_0.ObjectFactory;
import net.opengis.ows.v_1_1_0.UnNamedDomainType;
import net.opengis.ows.v_1_1_0.ValueType;
import net.opengis.wcs.v_1_1_2.AvailableKeys;
import net.opengis.wcs.v_1_1_2.AxisType;
import net.opengis.wcs.v_1_1_2.CoverageDescriptionType;
import net.opengis.wcs.v_1_1_2.CoverageDomainType;
import net.opengis.wcs.v_1_1_2.FieldType;
import net.opengis.wcs.v_1_1_2.GridCrsType;
import net.opengis.wcs.v_1_1_2.InterpolationMethodType;
import net.opengis.wcs.v_1_1_2.InterpolationMethods;
import net.opengis.wcs.v_1_1_2.RangeType;
import net.opengis.wcs.v_1_1_2.SpatialDomainType;
import net.opengis.wcs.v_1_1_2.TimePeriodType;
import net.opengis.wcs.v_1_1_2.TimeSequenceType;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jvnet.ogc.gml.v_3_1_1.jts.JTSToGML311CoordinateConverter;
import org.jvnet.ogc.gml.v_3_1_1.jts.JTSToGML311LinearRingConverter;
import org.jvnet.ogc.gml.v_3_1_1.jts.JTSToGML311PolygonConverter;
import org.jvnet.ogc.gml.v_3_1_1.jts.JTSToGML311SRSReferenceGroupConverter;

import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.edex.ogc.common.spatial.Composite3DBoundingBox;
import com.raytheon.uf.edex.ogc.common.spatial.CrsLookup;
import com.raytheon.uf.edex.ogc.common.spatial.VerticalCoordinate;
import com.raytheon.uf.edex.wcs.reg.CoverageDescription;
import com.raytheon.uf.edex.wcs.reg.RangeAxis;
import com.raytheon.uf.edex.wcs.reg.RangeField;
import com.raytheon.uf.edex.wcs.reg.RangeField.InterpolationType;
import com.raytheon.uf.edex.wcs.reg.RangeFieldDefinition;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Builds a Web Coverage Service describe coverage response object
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 12, 2013            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class DescCoverageBuilder {

    protected ObjectFactory owsFactory = new ObjectFactory();

    protected org.jvnet.ogc.gml.v_3_1_1.ObjectFactory gmlFactory = new org.jvnet.ogc.gml.v_3_1_1.ObjectFactory();

    protected List<String> formats;

    protected JTSToGML311PolygonConverter polyConverter;

    /**
     * @param formats
     */
    public DescCoverageBuilder(List<String> formats) {
        super();
        this.formats = formats;
        JTSToGML311SRSReferenceGroupConverter srsConv = new JTSToGML311SRSReferenceGroupConverter();
        JTSToGML311CoordinateConverter coordConv = new JTSToGML311CoordinateConverter(
                gmlFactory, srsConv);
        JTSToGML311LinearRingConverter ringConv = new JTSToGML311LinearRingConverter(
                gmlFactory, srsConv, coordConv);
        polyConverter = new JTSToGML311PolygonConverter(gmlFactory, srsConv,
                ringConv);
    }

    protected List<JAXBElement<?>> getBboxes(List<Composite3DBoundingBox> bboxes) {
        if (bboxes == null) {
            return null;
        }
        List<JAXBElement<?>> rval = new ArrayList<JAXBElement<?>>(bboxes.size());
        for (Composite3DBoundingBox from : bboxes) {
            BoundingBoxType to = owsFactory.createBoundingBoxType();
            ReferencedEnvelope horiz = from.getHorizontal();
            List<Double> lc = new ArrayList<Double>(3);
            List<Double> uc = new ArrayList<Double>(3);
            lc.add(horiz.getMinX());
            lc.add(horiz.getMinY());
            uc.add(horiz.getMaxX());
            uc.add(horiz.getMaxY());
            if (from.hasVertical()) {
                VerticalCoordinate vert = from.getVertical();
                lc.add(vert.getMin());
                uc.add(vert.getMax());
            }
            to.setUpperCorner(uc);
            to.setLowerCorner(lc);
            to.setCrs(CrsLookup.createCrsURN(from));
            to.setDimensions(BigInteger.valueOf(lc.size()));
            rval.add(owsFactory.createBoundingBox(to));
        }
        return rval;
    }

    protected PolygonType transform(Polygon p) {
        if (p == null) {
            return null;
        }
        return polyConverter.createGeometryType(p);
    }

    protected List<PolygonType> getPolys(Polygon... poly) {
        if (poly == null) {
            return null;
        }
        List<PolygonType> rval = new ArrayList<PolygonType>(poly.length);
        for (Polygon p : poly) {
            rval.add(transform(p));
        }
        return rval;
    }

    protected SpatialDomainType getSpatialDomain(CoverageDescription desc) {
        SpatialDomainType rval = new SpatialDomainType();
        List<JAXBElement<?>> bboxes = getBboxes(desc.getBboxes());
        if (bboxes != null) {
            rval.setBoundingBox(bboxes);
        }
        rval.setPolygon(getPolys(desc.getPolygon()));
        rval.setGridCRS(getGridCrs(desc));
        return rval;
    }

    /**
     * @param dimentions
     * @param gridBaseCrs
     * @return
     */
    protected GridCrsType getGridCrs(CoverageDescription desc) {
        GridCrsType rval = new GridCrsType();
        rval.setGridBaseCRS(desc.getGridBaseCrs());
        rval.setGridType(desc.getGridType());
        List<Double> gridOrigin = desc.getGridOrigin();
        if (gridOrigin != null) {
            rval.setGridOrigin(gridOrigin);
        }
        List<Double> gridOffsets = desc.getGridOffsets();
        if (gridOffsets != null) {
            rval.setGridOffsets(gridOffsets);
        }
        rval.setGridCS(desc.getGridCs());

        return rval;
    }

    private static final ThreadLocal<SimpleDateFormat> ISO_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat rval = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            rval.setTimeZone(TimeZone.getTimeZone("GMT"));
            return rval;
        }

    };

    protected TimePositionType transform(Date time) {
        TimePositionType rval = new TimePositionType();
        // client can't parse all ISO 8601, and it's our problem for some reason
        // String timeStr = LayerTransformer.format(time);
        String timeStr = ISO_FORMAT.get().format(time);
        rval.setValue(Arrays.asList(timeStr));
        return rval;
    }

    protected TimeSequenceType getTimeSequence(List<DataTime> times) {
        if (times == null) {
            return null;
        }
        TimeSequenceType timeSequence = new TimeSequenceType();
        List<Object> rval = new ArrayList<Object>(times.size());
        for (DataTime time : times) {
            if (time.getUtilityFlags().contains(DataTime.FLAG.PERIOD_USED)) {
                TimePeriodType tp = new TimePeriodType();
                TimeRange vp = time.getValidPeriod();
                tp.setBeginPosition(transform(vp.getStart()));
                tp.setEndPosition(transform(vp.getEnd()));
                tp.setTimeResolution("PT0S");
                rval.add(tp);
            } else {
                rval.add(transform(time.getRefTime()));
            }
        }
        timeSequence.setTimePositionOrTimePeriod(rval);
        return timeSequence;
    }

    protected CoverageDomainType getDomain(CoverageDescription desc) {
        CoverageDomainType coverageDomain = new CoverageDomainType();
        coverageDomain.setSpatialDomain(getSpatialDomain(desc));
        coverageDomain.setTemporalDomain(getTimeSequence(desc.getTimes()));
        return coverageDomain;
    }

    public CoverageDescriptionType getCoverageDescriptionType(
            CoverageDescription description) {
        CoverageDescriptionType cdt = new CoverageDescriptionType();
        cdt.setIdentifier(description.getIdentifier());
        cdt.setTitle(getAsLangString(description.getTitle()));
        cdt.setAbstract(getAsLangString(description.getAbstractStr()));
        if (description.getKeywords() != null) {
            cdt.setKeywords(getKeywords(description.getKeywords()));
        }
        cdt.setSupportedCRS(description.getCrs());
        cdt.setSupportedFormat(formats);
        cdt.setDomain(getDomain(description));
        cdt.setRange(getRange(description.getRangeFields()));
        return cdt;
    }

    protected ValueType getAsValue(Object obj) {
        if (obj == null) {
            return null;
        }
        ValueType rval = new ValueType();
        rval.setValue(obj.toString());
        return rval;
    }

    protected DomainMetadataType getUnits(String units) {
        if (units == null) {
            return null;
        }
        DomainMetadataType uom = new DomainMetadataType();
        uom.setValue(units);
        return uom;
    }

    protected AllowedValues getAllowedValues(RangeFieldDefinition def) {
        AllowedValues values = new AllowedValues();
        net.opengis.ows.v_1_1_0.RangeType range = new net.opengis.ows.v_1_1_0.RangeType();
        range.setMaximumValue(getAsValue(def.getMaxValue()));
        range.setMinimumValue(getAsValue(def.getMinValue()));
        values.setValueOrRange(Arrays.asList((Object) range));
        return values;
    }

    protected UnNamedDomainType getUnamed(RangeFieldDefinition def) {
        UnNamedDomainType rval = new UnNamedDomainType();
        if (def == null) {
            rval.setAnyValue(new AnyValue());
        } else {
            rval.setAllowedValues(getAllowedValues(def));
            rval.setUOM(getUnits(def.getUnits()));
        }
        return rval;
    }

    protected FieldType transform(RangeField from) {
        if (from == null) {
            return null;
        }
        FieldType rval = new FieldType();

        rval.setIdentifier(from.getIdentifier());
        rval.setDefinition(getUnamed(from.getDefinition()));
        rval.setInterpolationMethods(getInterps(from.getDefaultInterpolation(),
                from.getAdditionalInterpolations()));
        if (from.getAxis() != null) {
            rval.setAxis(getAxis(from.getAxis()));
        }
        if (from.getNullValue() != null) {
            rval.setNullValue(getNullValues(from.getNullValue()));
        }
        return rval;
    }

    /**
     * @param nullValue
     * @return
     */
    protected List<CodeType> getNullValues(String... nullValue) {
        if (nullValue == null) {
            return null;
        }
        List<CodeType> rval = new ArrayList<CodeType>(nullValue.length);
        for (String val : nullValue) {
            CodeType code = new CodeType();
            code.setValue(val);
            rval.add(code);
        }
        return rval;
    }

    protected AvailableKeys getKeys(Set<String> keys) {
        if (keys == null) {
            return null;
        }
        AvailableKeys rval = new AvailableKeys();
        rval.setKey(new ArrayList<String>(keys));
        return rval;
    }

    /**
     * @param axis
     * @return
     */
    private List<AxisType> getAxis(List<RangeAxis> axis) {
        if (axis == null) {
            return null;
        }
        List<AxisType> rval = new ArrayList<AxisType>(axis.size());
        for (RangeAxis ra : axis) {
            AxisType at = new AxisType();
            at.setIdentifier(ra.getIdentifier());
            at.setAvailableKeys(getKeys(ra.getKeys()));
            rval.add(at);
        }
        return rval;
    }

    protected List<InterpolationMethodType> transform(
            List<InterpolationType> from) {
        if (from == null) {
            return null;
        }
        List<InterpolationMethodType> rval = new ArrayList<InterpolationMethodType>(
                from.size());
        for (InterpolationType type : from) {
            InterpolationMethodType method = new InterpolationMethodType();
            method.setValue(type.toString());
            rval.add(method);
        }
        return rval;
    }

    /**
     * @param defaultInterpolation
     * @param additionalInterpolations
     * @return
     */
    protected InterpolationMethods getInterps(InterpolationType defaultInt,
            List<InterpolationType> additional) {
        InterpolationMethods rval = new InterpolationMethods();
        if (defaultInt == null) {
            defaultInt = InterpolationType.none;
        }
        rval.setDefault(defaultInt.toString());
        if (additional != null) {
            rval.setInterpolationMethod(transform(additional));
        }
        return rval;
    }

    /**
     * @param rangeFields
     * @return
     */
    protected RangeType getRange(List<RangeField> rangeFields) {
        if (rangeFields == null) {
            return null;
        }
        List<FieldType> rval = new ArrayList<FieldType>(rangeFields.size());
        for (RangeField rf : rangeFields) {
            rval.add(transform(rf));
        }
        RangeType rt = new RangeType();
        rt.setField(rval);
        return rt;
    }

}
