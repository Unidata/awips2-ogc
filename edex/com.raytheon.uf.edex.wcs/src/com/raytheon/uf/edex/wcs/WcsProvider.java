/*****************************************************************************************
 * COPYRIGHT (c), 2006, RAYTHEON COMPANY
 * ALL RIGHTS RESERVED, An Unpublished Work 
 *
 * RAYTHEON PROPRIETARY
 * If the end user is not the U.S. Government or any agency thereof, use
 * or disclosure of data contained in this source code file is subject to
 * the proprietary restrictions set forth in the Master Rights File.
 *
 * U.S. GOVERNMENT PURPOSE RIGHTS NOTICE
 * If the end user is the U.S. Government or any agency thereof, this source
 * code is provided to the U.S. Government with Government Purpose Rights.
 * Use or disclosure of data contained in this source code file is subject to
 * the "Government Purpose Rights" restriction in the Master Rights File.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * Use or disclosure of data contained in this source code file is subject to
 * the export restrictions set forth in the Master Rights File.
 ******************************************************************************************/

package com.raytheon.uf.edex.wcs;

import java.io.InputStream;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.ogc.common.OgcServiceInfo;
import com.raytheon.uf.edex.ogc.common.output.IOgcHttpResponse;
import com.raytheon.uf.edex.wcs.provider.OgcWcsProvider.WcsOpType;
import com.raytheon.uf.edex.wcs.request.DescCoverageRequest;
import com.raytheon.uf.edex.wcs.request.GetCapRequest;
import com.raytheon.uf.edex.wcs.request.GetCoverageRequest;
import com.raytheon.uf.edex.wcs.request.WcsRequest;

/**
 * Interface for OGC Web Coverage Service providers. Concrete providers
 * implement specific WCS specification versions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 19, 2013            bclement     Initial creation
 * </pre>
 * 
 */
public interface WcsProvider {

    public OgcResponse getCapabilities(OgcServiceInfo<WcsOpType> serviceinfo,
            GetCapRequest request);

    public OgcResponse describeCoverageType(
            OgcServiceInfo<WcsOpType> serviceinfo, DescCoverageRequest request);

    public void getCoverage(OgcServiceInfo<WcsOpType> serviceinfo,
            GetCoverageRequest request, IOgcHttpResponse response);

    public WcsRequest getRequest(InputStream in);

    public OgcResponse getError(WcsException e, MimeType exceptionFormat);

}
