/*******************************************************************************
 * Copyright (c) 2010, Hyper9 All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of the Hyper9 nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.hyper9.simdk.ws.util;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;

public class MorefUtil
{
    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(MorefUtil.class);

    public static String getMorefType(ManagedObjectReference moref)
        throws Exception
    {
        return getMorefClass(moref).getSimpleName();
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends ManagedObjectReference> getMorefClass(
        ManagedObjectReference moref) throws Exception
    {
        try
        {
            Session session =
                HibernateUtil.getSessionFactory().getCurrentSession();

            String sqlPatt = "SELECT %s FROM %s WHERE jpaId='%s'";
            String sql =
                String.format(
                    sqlPatt,
                    "C82d8ddd97d43e8cef8b7206f9cf1d100",
                    "ManagedObjectReference",
                    moref.getJpaId());
            SQLQuery sqlQuery = session.createSQLQuery(sql);
            sqlQuery.addScalar(
                "C82d8ddd97d43e8cef8b7206f9cf1d100",
                Hibernate.STRING);

            List sqlQueryResults = sqlQuery.list();

            String morefType = (String) sqlQueryResults.get(0);

            Class<? extends ManagedObjectReference> clazz =
                (Class<? extends ManagedObjectReference>) Class
                    .forName(String.format("%s.%s", 
                    ManagedObjectReference.class.getPackage().getName(), 
                    morefType));

            return clazz;
        }
        catch (Exception e)
        {
            logger.error("error getting moref class", e);
            throw e;
        }
    }

    public static ManagedObjectReference load(ManagedObjectReference moref)
        throws Exception
    {
        try
        {
            String jpaId = moref.getJpaId();
            Class<? extends ManagedObjectReference> morefClass =
                getMorefClass(moref);
            Session session =
                HibernateUtil.getSessionFactory().getCurrentSession();
            moref = (ManagedObjectReference) session.load(morefClass, jpaId);
            return moref;
        }
        catch (Exception e)
        {
            logger.error("error loading moref=" + moref.getJpaId(), e);
            throw e;
        }
    }
}
