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
package com.raytheon.uf.edex.ogc.common.reprojection;

import javax.media.jai.Interpolation;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Utility to facilitate lenient map operations during reprojection
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 06, 2014           ekladstrup  Initial creation
 * Aug 30, 2016  5867     randerso    Updated for GeoTools 15.1
 *
 * </pre>
 *
 * @author ekladstrup
 * @version 1.0
 */
public class DataReprojectorMapUtil {

    public static final Operations LENIENT_OPERATIONS = new Operations(
            new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));

    /**
     * Reproject a grid coverage into a different coordinate reference system
     * even when there is no information available for a datum shift
     *
     * @param srcCoverage
     *            the original grid coverage
     * @param targetCRS
     *            the target projection/coordinate system
     * @return a grid coverage in the new projection
     */
    public static GridCoverage2D lenientReprojectCoverage(
            GridCoverage2D srcCoverage, CoordinateReferenceSystem targetCRS) {

        return lenientReprojectCoverage(srcCoverage, targetCRS, null,
                Interpolation.getInstance(Interpolation.INTERP_NEAREST));
    }

    /**
     * Reproject a grid coverage into a different coordinate reference system
     * even when there is no information available for a datum shift
     *
     * @param srcCoverage
     *            the original grid coverage
     * @param targetCRS
     *            the target projection/coordinate system
     * @param targetGeometry
     *            the target grid geometry
     * @param interpolation
     *            String indication desired interpolation type: "nearest",
     *            "bilinear"
     * @return a grid coverage in the new projection
     */
    public static GridCoverage2D lenientReprojectCoverage(
            GridCoverage2D srcCoverage, CoordinateReferenceSystem targetCRS,
            GridGeometry targetGeometry, Interpolation interpolation) {
        return (GridCoverage2D) LENIENT_OPERATIONS.resample(srcCoverage,
                targetCRS, targetGeometry, interpolation);
    }

}
