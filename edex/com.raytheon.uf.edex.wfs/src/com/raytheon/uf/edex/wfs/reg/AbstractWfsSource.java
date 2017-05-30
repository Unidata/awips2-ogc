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
package com.raytheon.uf.edex.wfs.reg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.ogc.common.AbstractOgcSource;
import com.raytheon.uf.edex.ogc.common.OgcGeoBoundingBox;
import com.raytheon.uf.edex.ogc.common.OgcTimeRange;
import com.raytheon.uf.edex.wfs.WfsException;
import com.raytheon.uf.edex.wfs.WfsException.Code;
import com.raytheon.uf.edex.wfs.WfsFeatureType;
import com.raytheon.uf.edex.wfs.request.QualifiedName;
import com.raytheon.uf.edex.wfs.request.SortBy;

/**
 * Abstract base class for WFS sources
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- -------------------------
 * May 09, 2012           bclement  Initial creation
 * Oct 16, 2014  3454     bphillip  Upgrading to Hibernate 4
 * May 30, 2017  6186     rjpeter   Made getResource static.
 *
 * </pre>
 *
 * @author bclement
 * @param <T>
 */
public abstract class AbstractWfsSource<T> extends AbstractOgcSource
        implements IWfsSource {

    protected final String key;

    public static final String defaultCRS = "crs:84";

    public static final OgcGeoBoundingBox fullBbox = new OgcGeoBoundingBox(180,
            -180, 90, -90);

    protected abstract CoreDao getDao() throws Exception;

    protected static final String temporalKey = "dataTime.refTime";

    protected static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractWfsSource.class);

    protected IFeatureTypeModifier typeModifier = null;

    /**
     * @param key
     *            unique key for this source
     */
    public AbstractWfsSource(String key) {
        this.key = key;
    }

    @Override
    public List<WfsFeatureType> listFeatureTypes() {
        List<WfsFeatureType> featureTypes = getFeatureTypes();
        if (this.typeModifier == null) {
            return featureTypes;
        }
        return this.typeModifier.modify(featureTypes);
    }

    protected abstract List<WfsFeatureType> getFeatureTypes();

    @Override
    public List<WfsFeatureType> getAliases() {
        // Default to no aliases unless the source specifies
        return new ArrayList<>();
    }

    @Override
    public abstract String describeFeatureType(QualifiedName feature)
            throws WfsException;

    /**
     * Utility method for reading text files from the classpath
     *
     * @param loader
     * @param location
     * @return
     * @throws IOException
     */
    protected static String getResource(ClassLoader loader, String location)
            throws IOException {
        String rval;

        try (Scanner scanner = new Scanner(
                loader.getResourceAsStream(location))) {
            rval = scanner.useDelimiter("\\A").next();
        } catch (Throwable e) {
            throw new IOException(e);
        }

        return rval;
    }

    /**
     * Interacts with database to get features
     *
     * @param feature
     * @param query
     * @return
     * @throws WfsException
     */
    protected List<T> queryInternal(QualifiedName feature, WfsQuery query)
            throws WfsException {
        query = modQuery(query);
        List<T> rval;
        // TODO get rid of core DAO calls
        Session sess = null;
        try {
            CoreDao dao = getDao();
            sess = dao.getSessionFactory().openSession();
            Criteria criteria = sess.createCriteria(getFeatureEntity(feature));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            populateCriteria(criteria, query);
            criteria = modCriteria(criteria, query);
            criteria.setMaxResults(query.getMaxResults());
            List<SortBy> sortBys = query.getSortBys();
            addOrder(criteria, sortBys);

            rval = getResults(criteria);

        } catch (Exception e) {
            statusHandler.error("Problem querying for feature", e);
            throw new WfsException(Code.OperationProcessingFailed);
        } finally {
            if (sess != null) {
                sess.close();
            }
        }
        return rval;
    }

    protected List<T> getResults(Criteria criteria) {
        return criteria.list();
    }

    @Override
    public abstract String getFeatureSpatialField(QualifiedName feature);

    @Override
    public abstract Class<?> getFeatureEntity(QualifiedName feature);

    /**
     * Hook for implementing classes to modify the query object
     *
     * @param wfsq
     * @return
     */
    protected WfsQuery modQuery(WfsQuery wfsq) {
        return wfsq;
    }

    /**
     * Adds temporal criterion
     *
     * @param crit
     * @param query
     * @return
     */
    protected Criteria modCriteria(Criteria crit, WfsQuery query) {

        OgcTimeRange otr = query.timeRange;

        if (otr != null) {
            crit.add(Restrictions.between(temporalKey, otr.getStartTime(),
                    otr.getEndTime()));
        }

        return crit;
    }

    /**
     * @param criteria
     * @param sortBys
     */
    protected void addOrder(Criteria criteria, List<SortBy> sortBys) {
        if (sortBys == null || sortBys.isEmpty()) {
            return;
        }
        for (SortBy sb : sortBys) {
            switch (sb.getOrder()) {
            case Ascending:
                criteria.addOrder(Order.asc(sb.getProperty()));
                break;
            case Descending:
                criteria.addOrder(Order.desc(sb.getProperty()));
                break;
            default:
                statusHandler.warn("Unrecognized order: " + sb.getOrder());
            }
        }
    }

    /**
     * @param criteria
     * @param query
     */
    protected void populateCriteria(Criteria criteria, WfsQuery query) {
        query = modQuery(query);
        Criterion criterion = query.getCriterion();
        if (criterion != null) {
            criteria.add(criterion);
        }
        int maxResults = query.getMaxResults();
        if (maxResults > -1) {
            criteria.setMaxResults(maxResults);
        }
    }

    @Override
    public List<String> distinct(QualifiedName feature, WfsQuery query) {
        query = modQuery(query);
        List<String> rval;
        try {
            // List<?> res = getDao().queryByCriteria(query);
            // rval = new ArrayList<String>(res.size());
            // for (Object obj : res) {
            // ConvertUtil converter = BundleContextAccessor
            // .getService(ConvertUtil.class);
            // rval.add(converter.toString(obj));
            // }
            // If you want distinct querries, set this up.
            rval = null;
        } catch (Exception e) {
            statusHandler.error("Problem querying for record", e);
            rval = new ArrayList<>(0);
        }
        return rval;
    }

    @Override
    public long count(QualifiedName feature, WfsQuery query)
            throws WfsException {
        long rval;
        Session sess = null;
        try {
            CoreDao dao = getDao();
            SessionFactory sessFact = dao.getSessionFactory();
            sess = sessFact.openSession();
            Criteria criteria = sess.createCriteria(getFeatureEntity(feature));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            populateCriteria(criteria, query);
            criteria.setProjection(Projections.rowCount());
            List<Number> list = criteria.list();
            if (!list.isEmpty()) {
                rval = list.get(0).longValue();
            } else {
                rval = 0;
            }
        } catch (Exception e) {
            statusHandler.error("Unable to get count!", e);
            throw new WfsException(Code.OperationProcessingFailed);
        } finally {
            if (sess != null) {
                sess.close();
            }
        }
        return rval;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public abstract Class<?>[] getJaxbClasses();

    @Override
    public Map<String, String> getFieldMap() {
        return null;
    }

    /**
     * @return the typeModifier
     */
    public IFeatureTypeModifier getTypeModifier() {
        return typeModifier;
    }

    /**
     * @param typeModifier
     *            the typeModifier to set
     */
    public void setTypeModifier(IFeatureTypeModifier typeModifier) {
        this.typeModifier = typeModifier;
    }

}
