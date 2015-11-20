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

import java.awt.Point;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Arrays;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.datastorage.Request;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;

/**
 * Reprojection utility wrapper for ByteDataRecords
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 28, 2012            bclement     Initial creation
 * 
 * </pre>
 * 
 */
public class ByteDataReprojector extends
		AbstractDataReprojector<ByteDataRecord> {

	protected byte fill = 0;

    protected byte dataMaskValue = -1;

	@Override
	protected GridCoverage2D getGridCoverage(IDataRecord record,
            ReferencedEnvelope env) {
        ByteDataRecord dataRecord = (ByteDataRecord) record;
        byte[] data = dataRecord.getByteData();
        DataBufferByte buff = new DataBufferByte(data, data.length);
        int x = (int) dataRecord.getSizes()[0];
        int y = (int) dataRecord.getSizes()[1];
        CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
        return constructGridCoverage(crs.getName() + " Grid", buff, x, y, env);
	}

    @Override
    protected GridCoverage2D getMaskCoverage(IDataRecord record,
            ReferencedEnvelope env) {
        int x = (int) record.getSizes()[0];
        int y = (int) record.getSizes()[1];
        byte[] mask = new byte[x * y];
        Arrays.fill(mask, dataMaskValue);
        DataBufferByte buff = new DataBufferByte(mask, mask.length);
        CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
        return constructGridCoverage(crs.getName() + " Grid", buff, x, y, env);
    }

	@Override
	protected ByteDataRecord extractData(GridCoverage2D coverage) {
		RenderedImage image = coverage.getRenderedImage();
		Raster raster;
		if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
			// we can directly access data
			raster = image.getTile(0, 0);
		} else {
			// need to copy data out
			raster = image.getData();
		}
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		int height = raster.getHeight();
		int width = raster.getWidth();
		return new ByteDataRecord("", "", data, 2, new long[] { width, height });
	}

    @Override
    protected ByteDataRecord extractData(GridCoverage2D coverage,
            GridCoverage2D maskCoverage) {
        RenderedImage image = coverage.getRenderedImage();
        Raster raster;
        if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
            // we can directly access data
            raster = image.getTile(0, 0);
        } else {
            // need to copy data out
            raster = image.getData();
        }
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();

        // Extract mask
        image = maskCoverage.getRenderedImage();
        if (image.getNumXTiles() == 1 && image.getNumYTiles() == 1) {
            // we can directly access data
            raster = image.getTile(0, 0);
        } else {
            // need to copy data out
            raster = image.getData();
        }
        dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] mask = dataBuffer.getData();

        if (mask.length == data.length) {
            for (int i = 0; i < data.length; ++i) {
                if (mask[i] != dataMaskValue) {
                    data[i] = fill;
                }
            }
        }

        int height = raster.getHeight();
        int width = raster.getWidth();
        return new ByteDataRecord("", "", data, 2, new long[] { width, height });
    }

	@Override
	protected ByteDataRecord getDataSlice(IDataRecord record, Request req) {
		ByteDataRecord dataRecord = (ByteDataRecord) record;
		int[] max = req.getMaxIndexForSlab();
		int[] min = req.getMinIndexForSlab();
		int toWidth = max[0] - min[0];
		int toHeight = max[1] - min[1];
		byte[] from = dataRecord.getByteData();
		int fromWidth = (int) dataRecord.getSizes()[0];
		byte[] to = new byte[toWidth * toHeight];
		for (int fromY = min[1], toY = 0; fromY < max[1]; ++fromY, ++toY) {
			int toRow = toY * toWidth;
			int fromRow = fromY * fromWidth;
			for (int fromX = min[0], toX = 0; fromX < max[0]; ++fromX, ++toX) {
				to[toRow + toX] = from[fromRow + fromX];
			}
		}
		long[] sizes = { toWidth, toHeight };
		return new ByteDataRecord("", "", to, 2, sizes);
	}

	public byte getFill() {
		return fill;
	}

	public void setFill(byte fill) {
		this.fill = fill;
	}

	@Override
	protected boolean compatible(IDataRecord dataRecord) {
		return dataRecord instanceof ByteDataRecord;
	}

	@Override
	protected IDataRecord getDataPoints(IDataRecord record, Request req) {
		ByteDataRecord dataRecord = (ByteDataRecord) record;
		byte[] from = dataRecord.getByteData();
		int fromWidth = (int) dataRecord.getSizes()[0];
		Point[] points = req.getPoints();
		byte[] to = new byte[points.length];
		for (int i = 0; i < to.length; ++i) {
			Point p = points[i];
			to[i] = from[p.y * fromWidth + p.x];
		}
		return new ByteDataRecord("", "", to, 1, new long[] { to.length });
	}

}
