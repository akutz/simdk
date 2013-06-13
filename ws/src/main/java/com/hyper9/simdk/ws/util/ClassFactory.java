/*******************************************************************************
 * Copyright (c) 2010, Hyper9
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer. 
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *   
 * - Neither the name of the Hyper9 nor the names of its contributors may be 
 *   used to endorse or promote products derived from this software without 
 *   specific prior written permission. 
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
import org.hibernate.Query;
import org.hibernate.Session;
import com.hyper9.simdk.stubs.dao.DistributedVirtualSwitchHostProductSpec;
import com.hyper9.simdk.stubs.dao.DistributedVirtualSwitchProductSpec;
import com.hyper9.simdk.stubs.dao.ServiceEndpoint;
import com.hyper9.simdk.db.HibernateUtil;

public class ClassFactory
{
    public static List<ServiceEndpoint> getServiceEndpoints()
    {
        return ServiceInstanceUtil
            .getInternalServiceContent()
            .getServiceDirectory()
            .getService();
    }

    @SuppressWarnings("unchecked")
    public static DistributedVirtualSwitchHostProductSpec[] getDistributedVirtualSwitchHostProductSpecs()
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Query query =
            session
                .createQuery("FROM "
                    + DistributedVirtualSwitchHostProductSpec.class
                        .getSimpleName());

        List list = query.list();

        DistributedVirtualSwitchHostProductSpec[] arr =
            new DistributedVirtualSwitchHostProductSpec[list.size()];

        for (int x = 0; x < list.size(); ++x)
        {
            arr[x] = (DistributedVirtualSwitchHostProductSpec) list.get(x);
        }

        return arr;
    }

    @SuppressWarnings("unchecked")
    public static DistributedVirtualSwitchProductSpec[] getDistributedVirtualSwitchProductSpec()
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Query query =
            session.createQuery("FROM "
                + DistributedVirtualSwitchProductSpec.class.getSimpleName());

        List list = query.list();

        DistributedVirtualSwitchProductSpec[] arr =
            new DistributedVirtualSwitchProductSpec[list.size()];

        for (int x = 0; x < list.size(); ++x)
        {
            arr[x] = (DistributedVirtualSwitchProductSpec) list.get(x);
        }

        return arr;
    }
}
