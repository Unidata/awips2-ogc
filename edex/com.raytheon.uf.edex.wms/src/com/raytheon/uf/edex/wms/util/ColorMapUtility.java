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
package com.raytheon.uf.edex.wms.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IntegerDataRecord;
import com.raytheon.uf.common.datastorage.records.ShortDataRecord;

/**
 * Utility to apply AWIPS II colormaps to data records
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
public class ColorMapUtility {

    public static final double CALCULATED_PAD_RATIO = 0.25;

    public static ColorMapData buildColorMapData(IDataRecord dataRecord)
            throws Exception {
        long[] sizes = dataRecord.getSizes();
        int[] dims = { (int) sizes[0], (int) sizes[1] };
        ColorMapData rval = null;
        if (dataRecord instanceof ByteDataRecord) {
            ByteBuffer buff = ByteBuffer.wrap(((ByteDataRecord) dataRecord)
                    .getByteData());
            rval = new ColorMapData(buff, dims);
        } else if (dataRecord instanceof ShortDataRecord) {
            ShortBuffer buff = ShortBuffer.wrap(((ShortDataRecord) dataRecord)
                    .getShortData());
            rval = new ColorMapData(buff, dims);
        } else if (dataRecord instanceof IntegerDataRecord) {
            IntBuffer buff = IntBuffer.wrap(((IntegerDataRecord) dataRecord)
                    .getIntData());
            rval = new ColorMapData(buff, dims);
        } else if (dataRecord instanceof FloatDataRecord) {
            FloatBuffer buff = FloatBuffer.wrap(((FloatDataRecord) dataRecord)
                    .getFloatData());
            rval = new ColorMapData(buff, dims);
        } else {
            throw new IllegalArgumentException(
                    "Unable to apply colormap to class "
                            + dataRecord.getClass());
        }

        return rval;
    }

    /**
     * Returns an 2D GeneralEnvelope of data bounds. Dimension 1 contains the
     * values that map to the minimum and maximum of the colormap. Dimension 2
     * contains the actual minimum and maximum values in the data.
     * 
     * @param record
     *            IDataRecord
     * @return GeneralEnvelope
     */
    public static void calculateDataBounds(IDataRecord record, boolean doPad,
            ColorMapParameters params) {
        double dataMin;
        double dataMax;
        if (record instanceof ByteDataRecord) {
            dataMin = Byte.MAX_VALUE;
            dataMax = Byte.MIN_VALUE;
            byte[] data = ((ByteDataRecord) record).getByteData();
            for (byte b : data) {
                if (b > dataMax) {
                    dataMax = b;
                }
                if (b < dataMin) {
                    dataMin = b;
                }
            }
        } else if (record instanceof FloatDataRecord) {
            dataMin = Float.MAX_VALUE;
            dataMax = Float.MIN_VALUE;
            float[] data = ((FloatDataRecord) record).getFloatData();
            for (float b : data) {
                if (b > dataMax) {
                    dataMax = b;
                }
                if (b < dataMin) {
                    dataMin = b;
                }
            }
        } else if (record instanceof ShortDataRecord) {
            dataMin = Short.MAX_VALUE;
            dataMax = Short.MIN_VALUE;
            short[] data = ((ShortDataRecord) record).getShortData();
            for (short b : data) {
                if (b > dataMax) {
                    dataMax = b;
                }
                if (b < dataMin) {
                    dataMin = b;
                }
            }
        } else if (record instanceof IntegerDataRecord) {
            dataMin = Integer.MAX_VALUE;
            dataMax = Integer.MIN_VALUE;
            int[] data = ((IntegerDataRecord) record).getIntData();
            for (int b : data) {
                if (b > dataMax) {
                    dataMax = b;
                }
                if (b < dataMin) {
                    dataMin = b;
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported record class "
                    + record.getClass());
        }

        double cmapMin = dataMin;
        double cmapMax = dataMax;

        if (doPad) {
            double pad = (cmapMax - cmapMin) * CALCULATED_PAD_RATIO;
            cmapMin -= pad;
            cmapMax += pad;
        }

        params.setColorMapMin(new Double(cmapMin).floatValue());
        params.setColorMapMax(new Double(cmapMax).floatValue());
    }
}
