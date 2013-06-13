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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.stubs.faults.RuntimeFault;
import com.hyper9.simdk.stubs.mao.ContainerView;
import com.hyper9.simdk.stubs.mao.ListView;
import com.hyper9.simdk.stubs.mao.ManagedEntity;
import com.hyper9.simdk.stubs.mao.ManagedObject;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;

/**
 * A utility class for implementing the methods of the VI SDK ViewManager.
 * 
 * @author akutz
 * 
 */
public final class ViewManagerUtil
{
    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(ViewManagerUtil.class);

    public static ContainerView createContainerView(
        String sessionKey,
        ManagedObjectReference _this,
        ManagedObjectReference container,
        List<String> type,
        Boolean recursive) throws RemoteException, RuntimeFault
    {
        // Create a new ContainerView.
        ContainerView containerView = new ContainerView();
        containerView.setType("ContainerView");
        containerView.setServerGuid(ServiceInstanceUtil
            .getServiceInstance()
            .getServerGuid());
        containerView.setSessionKey(sessionKey);
        containerView.setRecursive(recursive);

        // Create and set the ContainerView's value.
        containerView.set_Value(String.format("[%s]%s", sessionKey, UUID
            .randomUUID()
            .toString()));

        // If the type parameter is not null then we need to assign it to the
        // ContainerView.
        if (type != null)
        {
            containerView.setManagedObjectType(type);
        }

        try
        {
            // Load the container from the database.
            container = MorefUtil.load(container);

            // Assign the container to the containerView.
            containerView.setContainer((ManagedEntity) container);
        }
        catch (Exception e)
        {
            logger.error("unable to load container from database", e);
            throw new RemoteException("unable to load container from database");
        }

        try
        {
            List<String> processed = new ArrayList<String>();

            traverseAndFindMOs((ManagedEntity) container, containerView
                .getView(), processed, recursive);
        }
        catch (Exception e)
        {
            logger.error("error traversing and finding managed objects", e);
            throw new RemoteException(
                "error traversing and finding managed objects");
        }

        ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getViewManager()
            .getViewList()
            .add(containerView);

        HibernateUtil.getSessionFactory().getCurrentSession().save(
            ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getViewManager());

        return containerView;
    }

    @SuppressWarnings("unchecked")
    private static void traverseAndFindMOs(
        ManagedObject toProcess,
        List<ManagedObject> found,
        List<String> processed,
        boolean recursive) throws Exception
    {
        // Get the value of the ManagedObject to process.
        String toProcessValue = toProcess.get_Value();

        // Check to see if this ManagedObject has been processed and return
        // early if it has.
        if (processed.contains(toProcessValue))
        {
            return;
        }

        // Note that this ManagedObject has been processed.
        processed.add(toProcessValue);

        BeanInfo beanInfo = Introspector.getBeanInfo(toProcess.getClass());
        PropertyDescriptor[] pdArr = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor pd : pdArr)
        {
            Class<?> propType = pd.getPropertyType();

            if (propType == List.class)
            {
                propType = TypeUtil.getGenericListType(pd.getReadMethod());
            }

            // If the property does not return a ManagedObject or a
            // list of ManagedObject then we do not care about it.
            if (!ManagedObject.class.isAssignableFrom(propType))
            {
                continue;
            }

            logger.debug("pd.name=" + pd.getName());

            Object pdVal = pd.getReadMethod().invoke(toProcess, new Object[0]);

            if (pdVal instanceof List)
            {
                List pdValList = (List) pdVal;

                for (Object o : pdValList)
                {
                    found.add((ManagedObject) o);

                    if (recursive)
                    {
                        traverseAndFindMOs(
                            (ManagedObject) o,
                            found,
                            processed,
                            recursive);
                    }
                }
            }
            else if (pdVal instanceof ManagedObject)
            {
                found.add((ManagedObject) pdVal);

                if (recursive)
                {
                    traverseAndFindMOs(
                        (ManagedObject) pdVal,
                        found,
                        processed,
                        recursive);
                }
            }
        }
    }

    public static ListView createListView(
        String sessionKey,
        ManagedObjectReference _this,
        List<ManagedObjectReference> obj) throws RemoteException, RuntimeFault
    {
        // Create a new ListView.
        ListView listView = new ListView();
        listView.setType("ListView");
        listView.setServerGuid(ServiceInstanceUtil
            .getServiceInstance()
            .getServerGuid());
        listView.setSessionKey(sessionKey);

        // Create and set the ListView's value.
        String listViewValue =
            String.format("[%s]%s", sessionKey, UUID.randomUUID().toString());
        listView.set_Value(listViewValue);

        // If we are passed a list of ManagedObjectReferences to create a list
        // out of then we need to process them.
        if (obj != null)
        {
            // Enumerate the ManagedObjectReferences in the object collection.
            for (ManagedObjectReference moref : obj)
            {
                try
                {
                    moref = MorefUtil.load(moref);
                }
                catch (Exception e)
                {
                    logger.error("unable to load moref", e);
                    throw new RemoteException("unable to load moref");
                }

                listView.getView().add((ManagedObject) moref);
            }
        }

        ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getViewManager()
            .getViewList()
            .add(listView);

        HibernateUtil.getSessionFactory().getCurrentSession().save(
            ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getViewManager());

        return listView;
    }
}
