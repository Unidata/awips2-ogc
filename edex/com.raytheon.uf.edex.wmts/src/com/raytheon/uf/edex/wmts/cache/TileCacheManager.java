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
package com.raytheon.uf.edex.wmts.cache;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.wmts.WmtsException;
import com.raytheon.uf.edex.wmts.WmtsException.Code;

/**
 * Manages access to cached tiles
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2012                    bclement     Initial creation
 * Nov 23, 2015 5087       bclement     refactor to have multiple implementations
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public abstract class TileCacheManager {

    protected static final boolean SKIP_CACHE = Boolean
            .getBoolean("wmts.skip.cache");

    protected static final int TIME_TO_LIVE = Integer.getInteger(
            "wmts.cache.ttl.seconds", 3600);

    public static final MimeType CACHE_FORMAT = new MimeType("image/png");

    protected static final IUFStatusHandler log = UFStatus
            .getHandler(TileCacheManager.class);

    /**
     * @param key
     * @param format
     * @return null if tile is not in cache
     * @throws WmtsException
     *             if tile cannot be returned in requested format
     */
    public byte[] getTile(String key, MimeType format) throws WmtsException {
        if (SKIP_CACHE) {
            return null;
        }
        byte[] rval = null;
        try {
            rval = read(key);
        } catch (IOException e) {
            log.error("Problem reading from file cache", e);
            // assume bad file, remove from cache
            remove(key);
            return null;
        }
        return convert(key, rval, format);
    }

    /**
     * Get tile as image data in CACHE_FORMAT
     * 
     * @param key
     * @return null if no tile found for key
     * @throws IOException
     */
    abstract protected byte[] read(String key) throws IOException;

    /**
     * Convert file in byte array to specified format
     * 
     * @param key
     *            key for tile
     * @param arr
     *            tile in cache format
     * @param format
     *            image mimetype
     * @return null if unable to convert
     * @throws WmtsException
     *             if requested format is not supported
     */
    protected byte[] convert(String key, byte[] arr, MimeType format)
            throws WmtsException {
        if (CACHE_FORMAT.equalsIgnoreParams(format)) {
            return arr;
        }
        Iterator<?> it = ImageIO.getImageWritersByMIMEType(format
                .toStringWithoutParams());
        if (!it.hasNext()) {
            // we should check way earlier than this
            throw new WmtsException(Code.InvalidParameterValue,
                    "Format not supported: " + format);
        }
        ImageWriter writer = (ImageWriter) it.next();
        byte[] rval = null;
        try (InputStream in = new ByteArrayInputStream(arr)) {
            BufferedImage img = ImageIO.read(in);
            rval = writeByteArray(img, writer);
        } catch (Exception e) {
            log.error("Problem converting image array", e);
            // assume that the array is bad and purge from cache
            remove(key);
        }
        return rval;
    }

    /**
     * Delete cached tile
     * 
     * @param key
     */
    abstract public void remove(String key);

    /**
     * Write image to byte array
     * 
     * @param img
     * @param writer
     *            writer for the desired image format
     * @return
     * @throws IOException
     */
    protected byte[] writeByteArray(BufferedImage img, ImageWriter writer)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream out = null;
        try {
            out = ImageIO.createImageOutputStream(baos);
            writer.setOutput(out);
            writer.write(img);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return baos.toByteArray();
    }

    /**
     * Store image data as CACHE_FORMAT
     * 
     * @param key
     * @param image
     */
    public void putTile(final String key, final BufferedImage image) {
        if (SKIP_CACHE) {
            return;
        }
        Iterator<?> it = ImageIO.getImageWritersByMIMEType(CACHE_FORMAT
                .toStringWithoutParams());
        if (!it.hasNext()) {
            log.error("Unable to write to cache format, no caching will be done");
            return;
        }
        ImageWriter writer = (ImageWriter) it.next();
        byte[] arr;
        try {
            arr = writeByteArray(image, writer);
        } catch (IOException e) {
            log.error("Unable to cache image for key: " + key, e);
            return;
        }
        try {
            write(key, arr);
        } catch (IOException e) {
            log.error("Unable to write to tile cache for key: " + key, e);
        }
    }

    /**
     * Internal method to store image data
     * 
     * @param key
     * @param arr
     * @throws IOException
     */
    abstract protected void write(String key, byte[] arr) throws IOException;

}
