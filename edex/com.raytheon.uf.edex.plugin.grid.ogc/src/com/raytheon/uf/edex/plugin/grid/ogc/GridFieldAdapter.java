/**
 * Copyright 09/24/12 Raytheon Company.
 *
 * Unlimited Rights
 * This software was developed pursuant to Contract Number 
 * DTFAWA-10-D-00028 with the US Government. The US Government’s rights 
 * in and to this copyrighted software are as specified in DFARS
 * 252.227-7014 which was made part of the above contract. 
 */
package com.raytheon.uf.edex.plugin.grid.ogc;

import java.util.Set;

import com.raytheon.uf.common.dataplugin.grid.GridRecord;
import com.raytheon.uf.common.parameter.mapping.ParameterMapper;
import com.raytheon.uf.edex.wcs.reg.IFieldAdapted;

/**
 * Maps grid Web Coverage Service parameters to/from cf conventions
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 5, 2013            bclement     Initial creation
 * 
 * </pre>
 * 
 * @author bclement
 * @version 1.0
 */
public class GridFieldAdapter implements IFieldAdapted<GridRecord> {

    public static final String BASE_CF_NS = "cf";

    public static final String EXTENDED_CF_NS = "cf-extended";

    @Override
    public String getCoverageField(GridRecord record) {
        String abbr = record.getInfo().getParameter().getAbbreviation();
        return getCFFromNCEP(abbr);
    }

    @Override
    public Class<GridRecord> getSupportedClass() {
        return GridRecord.class;
    }

    /**
     * Attempt to find Climate and Forecast conventional name for parameter
     * 
     * @param ncep
     * @return
     */
    public static final String getCFFromNCEP(String ncep) {
        String rval = ncep;
        ParameterMapper mapper = ParameterMapper.getInstance();
        Set<String> aliases = mapper.lookupAliasesOrEmpty(ncep, BASE_CF_NS);
        if (aliases.isEmpty()) {
            aliases = mapper.lookupAliasesOrEmpty(ncep, EXTENDED_CF_NS);
        }
        if (!aliases.isEmpty()) {
            rval = aliases.iterator().next();
        }
        return rval;
    }

    /**
     * Attempt to find parameter name for Climate and Forecast conventional name
     * 
     * @param cf
     * @return
     */
    public static final String getNCEPFromCF(String cf) {
        /*
         * TODO we just grab the first match here. We could do multiple DB
         * queries if we get multiple potential parameter names back, but that
         * would require a change to how WMS and WCS get records
         */
        String rval = cf;
        ParameterMapper mapper = ParameterMapper.getInstance();
        Set<String> baseNames = mapper.lookupBaseNamesOrEmpty(cf, BASE_CF_NS);
        if (baseNames.isEmpty()) {
            baseNames = mapper.lookupBaseNamesOrEmpty(cf, EXTENDED_CF_NS);
        }
        if (!baseNames.isEmpty()) {
            rval = baseNames.iterator().next();
        }
        return rval;
    }

}
