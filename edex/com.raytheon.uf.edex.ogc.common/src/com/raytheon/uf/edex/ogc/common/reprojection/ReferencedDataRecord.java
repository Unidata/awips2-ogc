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

import org.geotools.geometry.jts.ReferencedEnvelope;

import com.raytheon.uf.common.datastorage.records.IDataRecord;

/**
 * Bundles an IDataRecord object with spatial information
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
 * @author bclement
 * @version 1.0
 */
public class ReferencedDataRecord{	
	
	protected IDataRecord record;
	
	protected ReferencedEnvelope envelope;
	
	public ReferencedDataRecord(IDataRecord record, ReferencedEnvelope envlope) {
		this.record = record;
		this.envelope = envlope;
	}

	public IDataRecord getRecord() {
		return record;
	}

	public void setRecord(IDataRecord record) {
		this.record = record;
	}

	public ReferencedEnvelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(ReferencedEnvelope envelope) {
		this.envelope = envelope;
	}
	
}
