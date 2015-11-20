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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Tile cache that stores to the temp directory on the file system
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 23, 2015 5087       bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class FileTileCache extends TileCacheManager {

    protected static final File cacheDir = new File(
            System.getProperty("java.io.tmpdir"), "wmts-cache");

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    protected byte[] read(String key) throws IOException {
        File tileFile = new File(cacheDir, key);
        ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            byte[] rval = null;
            if (tileFile.exists()) {
                rval = Files.readAllBytes(tileFile.toPath());
            }
            return rval;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    protected void write(String key, byte[] arr) throws IOException {
        File tileFile = new File(cacheDir, key);
        WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (!tileFile.exists()) {
                File parent = tileFile.getParentFile();
                parent.mkdirs();
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(tileFile);
                    out.write(arr);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Remove any tiles that have are older than the current time-to-live value.
     */
    public void purgeExpiredCache() {
        if (cacheDir != null && cacheDir.isDirectory()) {
            purge(cacheDir);
        }
    }

    /**
     * Recursive method to remove tiles that are older than the current
     * time-to-live value. This does not remove empty directories.
     * 
     * @param parent
     */
    private void purge(File parent) {
        for (File f : parent.listFiles()) {
            if (f.exists()) {
                if (f.isDirectory()) {
                    purge(f);
                } else {
                    checkExpired(f);
                }
            }
        }
    }

    /**
     * Remove tile from cache if older than the current time-to-live value.
     * 
     * @param f
     */
    private void checkExpired(File f) {
        long diff = System.currentTimeMillis() - f.lastModified();
        if (diff > (TIME_TO_LIVE * 1000)) {
            deleteFile(f);
        }
    }

    /**
     * Delete file if it exists
     * 
     * @param f
     */
    private void deleteFile(File f) {
        WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (f.exists()) {
                if (!f.delete()) {
                    log.error("Unable to delete cache file: "
                            + f.getAbsolutePath());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(String key) {
        File tileFile = new File(cacheDir, key);
        deleteFile(tileFile);
    }

}
