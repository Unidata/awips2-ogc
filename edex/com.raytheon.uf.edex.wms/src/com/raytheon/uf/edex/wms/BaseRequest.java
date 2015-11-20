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
package com.raytheon.uf.edex.wms;

import com.raytheon.uf.common.http.MimeType;
import com.raytheon.uf.edex.ogc.common.OgcResponse;
import com.raytheon.uf.edex.ogc.common.OgcServiceInfo;
import com.raytheon.uf.edex.wms.IWmsProvider.WmsOpType;

/**
 * Base class for OGC Web Map Service requests
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
public class BaseRequest<T> {

    protected String version;

    protected MimeType format;

    protected String userName;

    protected String[] roles;

    protected MimeType exceptionFormat = OgcResponse.TEXT_XML_MIME;

    protected String updateSequence;

    protected OgcServiceInfo<T> serviceinfo;

    /**
	 * 
	 */
    public BaseRequest() {
    }

    public BaseRequest(String version, MimeType format, String userName,
            String[] roles) {
        super();
        this.version = version;
        this.format = format;
        this.userName = userName;
        this.roles = roles;
    }

    @SuppressWarnings("unchecked")
    public OgcResponse execute(IWmsProvider provider) {
        return provider.getCapabilities((BaseRequest<WmsOpType>) this);
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the format
     */
    public MimeType getFormat() {
        return format;
    }

    /**
     * @param format
     *            the format to set
     */
    public void setFormat(MimeType format) {
        this.format = format;
    }

    /**
     * @return the exceptionFormat
     */
    public MimeType getExceptionFormat() {
        return exceptionFormat;
    }

    /**
     * @param exceptionFormat
     *            the exceptionFormat to set
     */
    public void setExceptionFormat(MimeType exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
    }

    /**
     * @return the updateSequence
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * @param updateSequence
     *            the updateSequence to set
     */
    public void setUpdateSequence(String updateSequence) {
        this.updateSequence = updateSequence;
    }

    /**
     * @return the serviceinfo
     */
    public OgcServiceInfo<T> getServiceinfo() {
        return serviceinfo;
    }

    /**
     * @param serviceinfo
     *            the serviceinfo to set
     */
    public void setServiceinfo(OgcServiceInfo<T> serviceinfo) {
        this.serviceinfo = serviceinfo;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the roles
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * @param roles
     *            the roles to set
     */
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

}
