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
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentIdentifierBag;
import com.hyper9.simdk.stubs.ArrayOf;
import com.hyper9.simdk.stubs.dao.DynamicProperty;
import com.hyper9.simdk.stubs.dao.LocalizedMethodFault;
import com.hyper9.simdk.stubs.dao.MissingProperty;
import com.hyper9.simdk.stubs.dao.ObjectContent;
import com.hyper9.simdk.stubs.dao.ObjectSpec;
import com.hyper9.simdk.stubs.dao.ObjectUpdate;
import com.hyper9.simdk.stubs.dao.PropertyChange;
import com.hyper9.simdk.stubs.dao.PropertyFilterSpec;
import com.hyper9.simdk.stubs.dao.PropertyFilterUpdate;
import com.hyper9.simdk.stubs.dao.PropertySpec;
import com.hyper9.simdk.stubs.dao.SelectionSpec;
import com.hyper9.simdk.stubs.dao.TraversalSpec;
import com.hyper9.simdk.stubs.dao.UpdateSet;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.stubs.enums.PropertyChangeOp;
import com.hyper9.simdk.stubs.faults.InvalidCollectorVersion;
import com.hyper9.simdk.stubs.faults.InvalidProperty;
import com.hyper9.simdk.stubs.faults.RuntimeFault;
import com.hyper9.simdk.stubs.mao.ArrayOfManagedObjectReference;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;
import com.hyper9.simdk.stubs.mao.PropertyFilter;

public class PropertyCollectorUtil
{
    /**
     * The logger.
     */
    private static Logger logger =
        Logger.getLogger(PropertyCollectorUtil.class);

    /*
     * if (packageName.equals("java.lang")) { packageName =
     * "com.hyper9.simdk.dao"; }
     * 
     * if (packageName.equals("com.hyper9.simdk.mao") &&
     * !listTypeSimpleName.equals("ManagedObjectReference")) { String
     * arrayOfName = "com.hyper9.simdk.mao.ArrayOfManagedObjectReference";
     */
    /**
     * The name of the data objects package.
     */
    private static String DAO_PACKAGE_NAME =
        DynamicProperty.class.getPackage().getName();

    /**
     * The name of the managed objects package.
     */
    private static String MAO_PACKAGE_NAME =
        ManagedObjectReference.class.getPackage().getName();

    /**
     * The simple name of the ManagedObjectReference class.
     */
    private static String MOREF_CLASS_NAME =
        ManagedObjectReference.class.getSimpleName();

    /**
     * The full name of the ArrayOfManagedObjectReference class.
     */
    private static String ARR_MOREF_CLASS_NAME =
        ArrayOfManagedObjectReference.class.getName();

    public static PropertyFilter createFilter(
        String sessionKey,
        ManagedObjectReference _this,
        PropertyFilterSpec spec,
        Boolean partialUpdates)
        throws RemoteException,
        InvalidProperty,
        RuntimeFault
    {
        PropertyFilter propFilter = new PropertyFilter();
        propFilter.set_Value(String.format("[%s]%s", sessionKey, UUID
            .randomUUID()
            .toString()));
        propFilter.setPartialUpdates(partialUpdates);
        propFilter.setType("PropertyFilter");

        List<ObjectSpec> objSpecList = spec.getObjectSet();

        for (ObjectSpec objSpec : objSpecList)
        {
            try
            {
                ManagedObjectReference loadedMoref =
                    MorefUtil.load(objSpec.getObj());
                objSpec.setObj(loadedMoref);
            }
            catch (Exception e)
            {
                logger
                    .error("unable to load object in object specification", e);
                throw new RemoteException(
                    "unable to load object in object specification");
            }
        }

        propFilter.setSpec(spec);

        propFilter.setServerGuid(ServiceInstanceUtil
            .getServiceInstance()
            .getServerGuid());
        propFilter.setSessionKey(sessionKey);

        ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getPropertyCollector()
            .getFilter()
            .add(propFilter);

        HibernateUtil.getSessionFactory().getCurrentSession().save(
            ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getPropertyCollector());

        return propFilter;
    }

    public static List<ObjectContent> retrieveProperties(
        ManagedObjectReference _this,
        List<PropertyFilterSpec> specSet)
        throws RemoteException,
        InvalidProperty,
        RuntimeFault
    {
        // Create an ObjectContent list to put all of the processed objects
        // into.
        List<ObjectContent> objContList = new ArrayList<ObjectContent>();

        // Process each PropertyFilterSpec
        for (PropertyFilterSpec propFilterSpec : specSet)
        {
            processPropertFilterSpec(objContList, propFilterSpec);
        }

        // Return the ObjectContent array.
        return objContList;
    }

    private static void processPropertFilterSpec(
        List<ObjectContent> objContList,
        PropertyFilterSpec propFilterSpec)
        throws RemoteException,
        InvalidProperty,
        RuntimeFault
    {
        // Get the PropertyFilterSpec's ObjectSpec array.
        List<ObjectSpec> objSpecList = propFilterSpec.getObjectSet();

        // Get the PropertyFilterSpec's PropertySpec array.
        List<PropertySpec> propSpecList = propFilterSpec.getPropSet();

        // Process each ObjectSpec
        for (int objSpecArrIdx = 0; objSpecArrIdx < objSpecList.size(); ++objSpecArrIdx)
        {
            // Get the current ObjectSpec.
            ObjectSpec objSpec = objSpecList.get(objSpecArrIdx);

            // Get the object that the ObjectSpec references.
            ManagedObjectReference objMoref = objSpec.getObj();

            // Get the array of the ObjectSpec's SelectionSpecs.
            List<SelectionSpec> selectSpecList = objSpec.getSelectSet();

            List<ManagedObjectReference> collectedMorefs =
                new ArrayList<ManagedObjectReference>();

            try
            {
                HashMap<String, TraversalSpec> cache = null;

                if (selectSpecList != null)
                {
                    // Create a cache to hold the named TraversalSpec
                    // references.
                    cache = new HashMap<String, TraversalSpec>();

                    // Cache the named TraversalSpec references.
                    cacheNamedSelectionSpecs(selectSpecList, cache);
                }

                // Collect the objects to process.
                collectObjectsToProcess(
                    objMoref,
                    objSpec.getSkip(),
                    selectSpecList,
                    cache,
                    collectedMorefs);

                // Process all of the managed object references we collected.
                for (ManagedObjectReference moref : collectedMorefs)
                {
                    processObject(objContList, moref, propSpecList);
                }
            }
            catch (Exception e)
            {
                logger.error("unable to retrieve properties", e);
                throw new RemoteException("unable to retrieve properties");
            }
        }
    }

    private static void collectObjectsToProcess(
        ManagedObjectReference objMoref,
        Boolean skip,
        List<SelectionSpec> selectSpecList,
        HashMap<String, TraversalSpec> cache,
        List<ManagedObjectReference> collectedMorefs) throws Exception
    {
        if (skip == null || !skip)
        {
            collectedMorefs.add(objMoref);
        }

        if (selectSpecList == null)
        {
            return;
        }

        for (SelectionSpec ss : selectSpecList)
        {
            collectObjectsToProcess(objMoref, skip, ss, cache, collectedMorefs);
        }
    }

    @SuppressWarnings("unchecked")
    private static void collectObjectsToProcess(
        ManagedObjectReference objMoref,
        Boolean skip,
        SelectionSpec selectSpec,
        HashMap<String, TraversalSpec> cache,
        List<ManagedObjectReference> collectedMorefs) throws Exception
    {
        TraversalSpec travSpec = null;

        // If the SelectionSpec parameter is already a TraversalSpec then
        // assign it to the travSpec variable.
        if (selectSpec instanceof TraversalSpec)
        {
            logger.debug("selectSpec already a TraversalSpec");
            travSpec = (TraversalSpec) selectSpec;
        }

        // If the SelectionSpec parameter has a valid name property check to see
        // if we can obtain a cached TraversalSpec with said name.
        else if (selectSpec.getName() != null
            && cache.containsKey(selectSpec.getName()))
        {
            logger.debug("getting TraversalSpec from cache by name="
                + selectSpec.getName());
            travSpec = cache.get(selectSpec.getName());
        }

        // At this point we have no TraversalSpec so we should just return.
        else
        {
            return;
        }

        logger.debug("has TraversalSpec");

        // Load the ManagedObjectReference.
        objMoref = MorefUtil.load(objMoref);

        String morefType = MorefUtil.getMorefType(objMoref);

        if (!travSpec.getType().equals(morefType))
        {
            logger.debug("not collecting moref of type=" + morefType
                + ", expecting type=" + travSpec.getType());
            return;
        }

        // Get the name of the property to follow for additional managed object
        // references.
        String propPath = travSpec.getPath();

        logger.debug("propPath=" + propPath);

        // Get the property path queue.
        Queue<String> propPathQueue = toQueue(propPath);

        // Get the property path value.
        Object propPathValue =
            getOneOrMoreMorefsToTraverse(objMoref, propPathQueue);

        if (propPathValue == null)
        {
            logger.debug("propPathValue is null");
            return;
        }

        // If the value is an array then consider it to be an array of
        // ManagedObjectReferences.
        if (propPathValue instanceof List)
        {
            logger.debug("propPathValue is a List");

            // propPathValue = fromBag((List) propPathValue);

            List propPathValueList = (List) propPathValue;

            // Process each managed object reference.
            for (Object o : propPathValueList)
            {
                ManagedObjectReference moref = (ManagedObjectReference) o;

                collectObjectsToProcess(moref, travSpec.getSkip(), travSpec
                    .getSelectSet(), cache, collectedMorefs);
            }
        }

        // If the value is not an array then it is a single
        // MangagedObjectReference.
        else if (propPathValue instanceof ManagedObjectReference)
        {
            logger.debug("propPathValue is a moref");

            ManagedObjectReference moref =
                (ManagedObjectReference) propPathValue;

            // Process the managed object reference.
            collectObjectsToProcess(moref, travSpec.getSkip(), travSpec
                .getSelectSet(), cache, collectedMorefs);
        }

        else
        {
            logger.debug("propPathValue was not handled="
                + propPathValue.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private static List fromBag(List bag)
    {
        if (bag instanceof PersistentIdentifierBag)
        {
            return fromPersistentIdBag((PersistentIdentifierBag) bag);
        }
        else if (bag instanceof PersistentBag)
        {
            return fromPersistentBag((PersistentBag) bag);
        }

        return bag;
    }

    @SuppressWarnings("unchecked")
    private static List fromPersistentIdBag(PersistentIdentifierBag bag)
    {
        List<Object> listFromBag = new ArrayList<Object>();

        int size = bag.size();

        for (int x = 0; x < size; ++x)
        {
            Object el = bag.get(x);
            listFromBag.add(el);
        }

        return listFromBag;
    }

    @SuppressWarnings("unchecked")
    private static List fromPersistentBag(PersistentBag bag)
    {
        List<Object> listFromBag = new ArrayList<Object>();

        int size = bag.size();

        for (int x = 0; x < size; ++x)
        {
            Object el = bag.get(x);
            listFromBag.add(el);
        }

        return listFromBag;
    }

    /**
     * This function recursively enumerates the given SelectionSpec array,
     * caching any named TraversalSpec objects it discovers along the way.
     * 
     * @param selectSpecList The SelectionSpec array to recursively enumerate.
     * @param cache The named TraversalSpec cache.
     */
    private static void cacheNamedSelectionSpecs(
        List<SelectionSpec> selectSpecList,
        HashMap<String, TraversalSpec> cache)
    {
        // If the SelectionSpec array is null we obviously do not need to
        // enumerate it.
        if (selectSpecList == null)
        {
            return;
        }

        // For each SelectionSpec in the array test it to see if it is a
        // TraversalSpec and if so then we should cache it and then recursively
        // call this function on the TraversalSpec's own selectSet property.
        for (SelectionSpec ss : selectSpecList)
        {
            if (ss instanceof TraversalSpec && ss.getName() != null)
            {
                TraversalSpec travSpec = (TraversalSpec) ss;
                cache.put(travSpec.getName(), travSpec);
                cacheNamedSelectionSpecs(travSpec.getSelectSet(), cache);
            }
        }
    }

    private static void processObject(
        List<ObjectContent> objContList,
        ManagedObjectReference objMoref,
        List<PropertySpec> propSpecList) throws Exception
    {
        // Process each PropertySpec
        for (int propSpecArrIdx = 0; propSpecArrIdx < propSpecList.size(); ++propSpecArrIdx)
        {
            // Get the current PropertySpec.
            PropertySpec propSpec = propSpecList.get(propSpecArrIdx);

            // If this PropertySpec is valid for the type of the current
            // object then process it.
            if (propSpec.getType().equals(MorefUtil.getMorefType(objMoref)))
            {
                try
                {
                    processPropertySpec(objContList, objMoref, propSpec);
                }
                catch (Exception e)
                {
                    logger.error("unable to process property specification", e);
                    throw new RemoteException(e.getMessage());
                }
            }
        }
    }

    private static void processPropertySpec(
        List<ObjectContent> objContList,
        ManagedObjectReference objMoref,
        PropertySpec propSpec) throws Exception
    {
        // Load the ManagedObjectReference.
        objMoref = MorefUtil.load(objMoref);

        // Introspect the class.
        BeanInfo beanInfo = TypeUtil.getBeanInfo(objMoref);

        // Get a list of the class's properties.
        PropertyDescriptor[] pdList = beanInfo.getPropertyDescriptors();

        if (propSpec.getAll() != null && propSpec.getAll())
        {
            getAllProperties(objContList, objMoref, pdList);
        }
        else
        {
            // Get the property paths to retrieve.
            List<String> propPathList = propSpec.getPathSet();

            if (propPathList == null)
            {
                return;
            }

            // Create a list to hold the property values.
            List<DynamicProperty> propVals = new ArrayList<DynamicProperty>();

            // Create a list to hold any missing properties.
            List<MissingProperty> missingProps =
                new ArrayList<MissingProperty>();

            // Enumerate the property paths.
            for (String propPath : propPathList)
            {
                // Create a Queue to push the property path parts onto.
                Queue<String> propPathPartQueue = toQueue(propPath);

                // Get the property's value.
                try
                {
                    DynamicProperty dp =
                        getDynamicProperty(objMoref, propPathPartQueue);
                    dp.setName(propPath);

                    if (dp.getVal() == null)
                    {
                        MissingProperty mp = new MissingProperty();
                        mp.setPath(propPath);
                        missingProps.add(mp);
                    }
                    else
                    {
                        propVals.add(dp);
                    }
                }
                catch (InvalidProperty ex)
                {
                    LocalizedMethodFault lmf = new LocalizedMethodFault();
                    lmf.setFault(ex);

                    MissingProperty mp = new MissingProperty();
                    mp.setPath(propPath);
                    mp.setFault(lmf);

                    missingProps.add(mp);
                }
            }

            ObjectContent objCont = new ObjectContent();
            objCont.setObj(objMoref);
            objCont.setPropSet(propVals);
            objCont.setMissingSet(missingProps);

            objContList.add(objCont);
        }
    }

    /**
     * This function gets one or more ManagedObjectReferences by looking at a
     * current object and then following the property path in the given queue.
     * 
     * @param currentObj The current object.
     * @param propPathPartQueue The property path to follow.
     * @return One or more ManagedObjectReferences.
     * @throws Exception When an error occurs.
     */
    private static Object getOneOrMoreMorefsToTraverse(
        Object currentObj,
        Queue<String> propPathPartQueue) throws Exception
    {
        String propPart = propPathPartQueue.remove();

        logger.debug("getting property=" + propPart);

        // Build the name of the property's getter method.
        String getterName = "get" + upCaseFirstChar(propPart);

        logger.debug("built getter name=" + getterName);

        // Get the property's getter method.
        Method getterMethod;

        getterMethod =
            currentObj.getClass().getMethod(getterName, new Class<?>[0]);

        // Get the property value.
        Object propVal = getterMethod.invoke(currentObj, new Object[0]);

        if (propPathPartQueue.size() > 0)
        {
            return getOneOrMoreMorefsToTraverse(propVal, propPathPartQueue);
        }
        else
        {
            return propVal;
        }
    }

    @SuppressWarnings("unchecked")
    private static DynamicProperty getDynamicProperty(
        Object currentObj,
        Queue<String> propPathPartQueue) throws Exception
    {
        String propPart = propPathPartQueue.remove();

        logger.debug("getting property part=" + propPart);

        // Define a pattern to parse the property part.
        Pattern propPartPatt =
            Pattern.compile("([^\\]]*?)\\[\\\"?([^\\\"\\]]*?)\\\"?\\]");

        // Attempt to match the property part.
        Matcher propPartMatcher = propPartPatt.matcher(propPart);

        // Define the name of the property.
        String propName = propPart;

        // Define the property index's key value (if any).
        String keyVal = null;

        if (propPartMatcher.matches())
        {
            logger.debug("property part is indexed");

            propName = propPartMatcher.group(1);
            keyVal = propPartMatcher.group(2);

            logger.debug("property part index name=" + propName);
            logger.debug("property part index key=" + keyVal);
        }

        // Build the name of the property's getter method.
        String getterName = "get" + upCaseFirstChar(propName);

        logger.debug("built getter name=" + getterName);

        // Get the property's getter method.
        Method getterMethod;

        try
        {
            getterMethod =
                currentObj.getClass().getMethod(getterName, new Class<?>[0]);
        }
        catch (Exception e)
        {
            InvalidProperty ex = new InvalidProperty();
            ex.setName(propPart);
            throw ex;
        }

        // Should we ignore this property?
        if (shouldIgnoreProperty(getterMethod))
        {
            InvalidProperty ex = new InvalidProperty();
            ex.setName(propPart);
            throw ex;
        }

        // Get the property value.
        Object propVal = getterMethod.invoke(currentObj, new Object[0]);

        if (propVal == null
            || (propVal instanceof List && ((List) propVal).size() == 0))
        {
            return new DynamicProperty();
        }
        else if (propPathPartQueue.size() > 0)
        {
            if (propVal instanceof List && keyVal != null)
            {
                propVal = getArrayValByKey((List) propVal, keyVal);
            }

            return getDynamicProperty(propVal, propPathPartQueue);
        }
        else
        {
            // Pre-fetch the data from Hibernate.
            // prefetch(propVal);

            if (propVal instanceof List && keyVal != null)
            {
                propVal = getArrayValByKey((List) propVal, keyVal);
            }

            // If the property value is an array then we need to put into a
            // backing array object.
            else if (propVal instanceof List)
            {
                propVal = processList(getterMethod, (List) propVal);
            }

            DynamicProperty dp = new DynamicProperty();
            dp.setVal(propVal);
            return dp;
        }
    }

    @SuppressWarnings("unchecked")
    private static ArrayOf processList(Method getterMethod, List toProcess)
        throws Exception
    {
        Class<?> listType = TypeUtil.getGenericListType(getterMethod);

        String listTypeSimpleName = listType.getSimpleName();
        if (listTypeSimpleName.equals("Integer"))
        {
            listTypeSimpleName = "Int";
        }

        String packageName = listType.getPackage().getName();

        if (packageName.equals("java.lang"))
        {
            packageName = DAO_PACKAGE_NAME;
        }

        if (packageName.equals(MAO_PACKAGE_NAME)
            && !listTypeSimpleName.equals(MOREF_CLASS_NAME))
        {
            String arrayOfName = ARR_MOREF_CLASS_NAME;

            Class<? extends ArrayOf> arrayOfClass =
                (Class<? extends ArrayOf>) Class.forName(arrayOfName);

            Constructor<? extends ArrayOf> arrayOfCtor =
                arrayOfClass.getConstructor(new Class<?>[0]);

            ArrayOf arrayOfObj = arrayOfCtor.newInstance(new Object[0]);

            List<ManagedObjectReference> morefList =
                new ArrayList<ManagedObjectReference>();

            for (Object o : toProcess)
            {
                morefList.add((ManagedObjectReference) o);
            }

            arrayOfObj.setData(morefList);

            return arrayOfObj;
        }
        else
        {
            String arrayOfName = packageName + ".ArrayOf" + listTypeSimpleName;

            Class<? extends ArrayOf> arrayOfClass =
                (Class<? extends ArrayOf>) Class.forName(arrayOfName);

            Constructor<? extends ArrayOf> arrayOfCtor =
                arrayOfClass.getConstructor(new Class<?>[0]);

            ArrayOf arrayOfObj = arrayOfCtor.newInstance(new Object[0]);

            arrayOfObj.setData(toProcess);

            return arrayOfObj;
        }
    }

    @SuppressWarnings("unchecked")
    private static void prefetch(Object object) throws Exception
    {
        if (object == null)
        {
            return;
        }

        if (object instanceof List)
        {
            prefetch((List) object);
            return;
        }

        BeanInfo beanInfo = TypeUtil.getBeanInfo(object);
        PropertyDescriptor[] pdArr = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor pd : pdArr)
        {
            if (shouldIgnoreProperty(pd.getReadMethod()))
            {
                continue;
            }

            try
            {
                if (object instanceof ManagedObjectReference)
                {
                    if (pd.getName().matches("serverGuid|type"))
                    {
                        pd.getReadMethod().invoke(object, new Object[0]);
                    }
                }
                else
                {
                    Object pdVal =
                        pd.getReadMethod().invoke(object, new Object[0]);
                    prefetch(pdVal);
                }
            }
            catch (Exception e)
            {
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void prefetch(List list) throws Exception
    {
        list = fromBag(list);

        for (Object o : list)
        {
            prefetch(o);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getArrayValByKey(List list, String key)
        throws Exception
    {
        for (Object o : list)
        {
            Object keyVal = getKeyVal(o);
            String skeyVal = String.valueOf(keyVal);
            if (skeyVal.equals(key))
            {
                return o;
            }
        }

        return null;
    }

    private static Object getKeyVal(Object obj) throws Exception
    {
        Method m = obj.getClass().getMethod("getKey", new Class<?>[0]);
        Object keyVal = m.invoke(obj, new Object[0]);
        return keyVal;
    }

    /**
     * Get all the properties of a given ManagedObjectReference.
     * 
     * @param objContList
     * @param objMoref
     * @param pdList
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unchecked")
    private static void getAllProperties(
        List<ObjectContent> objContList,
        ManagedObjectReference objMoref,
        PropertyDescriptor[] pdList) throws Exception
    {
        List<DynamicProperty> dynPropList = new ArrayList<DynamicProperty>();

        for (PropertyDescriptor pd : pdList)
        {
            if (shouldIgnoreProperty(pd.getReadMethod()))
            {
                continue;
            }

            Object propVal = pd.getReadMethod().invoke(objMoref, new Object[0]);

            if (propVal == null)
            {
                continue;
            }

            // prefetch(propVal);

            if (propVal instanceof List)
            {
                propVal = processList(pd.getReadMethod(), (List) propVal);
            }

            DynamicProperty dp = new DynamicProperty();
            dp.setName(pd.getName());
            dp.setVal(propVal);
            dynPropList.add(dp);
        }

        ObjectContent objCont = new ObjectContent();
        objCont.setObj(objMoref);
        objCont.setPropSet(dynPropList);

        objContList.add(objCont);
    }

    /**
     * Gets a flag indicating whether or not the property should be ignored
     * during property collection.
     * 
     * @param propType The property type.
     * @return A flag indicating whether or not the property should be ignored
     *         during property collection.
     */
    private static boolean shouldIgnoreProperty(Method method) throws Exception
    {
        if (method == null)
        {
            return true;
        }

        Class<?> propType = method.getReturnType();

        if (method.getName().equals("getHibernateLazyInitializer"))
        {
            return true;
        }

        if (method.getName().equals("getJpaId"))
        {
            return true;
        }

        if (method.getName().equals("get_Value"))
        {
            return true;
        }

        if (method.getName().equals("getSessionKey"))
        {
            return true;
        }

        // Ignore the Class class.
        if (propType == Class.class)
        {
            return true;
        }

        return false;
    }

    private static String upCaseFirstChar(String toUpCase)
    {
        return toUpCase.substring(0, 1).toUpperCase() + toUpCase.substring(1);
    }

    /**
     * Gets a queue from a property path.
     * 
     * @param propPath The property path.
     * @return A queue from the property path.
     */
    private static Queue<String> toQueue(String propPath)
    {
        String[] parts = propPath.split("\\.");
        return toQueue(parts);
    }

    /**
     * Converts an array of T into a Queue of T.
     * 
     * @param <T> The T element.
     * @param array An array of T.
     * @return A queue of T.
     */
    private static <T> Queue<T> toQueue(T[] array)
    {
        Queue<T> q = new LinkedList<T>();

        for (T a : array)
        {
            q.add(a);
        }

        return q;
    }

    private static int updated = 0;

    public static UpdateSet waitForUpdates(
        String sessionKey,
        ManagedObjectReference _this,
        String version)
        throws RemoteException,
        InvalidCollectorVersion,
        RuntimeFault
    {

        try
        {
            if (updated > 10)
            {
                Thread.sleep(1000 * 10);
            }
            else
            {
                Thread.sleep(1000 * 2);
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }

        ++updated;
        return getUpdateSet(sessionKey, version);
    }

    public static UpdateSet checkForUpdates(
        String sessionKey,
        ManagedObjectReference _this,
        String version)
        throws RemoteException,
        InvalidCollectorVersion,
        RuntimeFault
    {
        return getUpdateSet(sessionKey, version);
    }

    private static HashMap<String, List<String>> returnedProps =
        new HashMap<String, List<String>>();

    private static UpdateSet getUpdateSet(String sessionKey, String version)
        throws RemoteException,
        InvalidCollectorVersion,
        RuntimeFault
    {
        UpdateSet us = new UpdateSet();
        us.setVersion(incrementVersion(version));

        List<PropertyFilter> propFilters = getPropertyFilters(sessionKey);

        for (PropertyFilter pf : propFilters)
        {
            PropertyFilterUpdate pfu = new PropertyFilterUpdate();
            pfu.setFilter(pf);

            try
            {
                List<ObjectContent> objContList =
                    new ArrayList<ObjectContent>();

                PropertyCollectorUtil.processPropertFilterSpec(objContList, pf
                    .getSpec());

                for (ObjectContent objCon : objContList)
                {
                    ObjectUpdate ou = new ObjectUpdate();
                    ou.setObj(objCon.getObj());

                    List<String> returnedPropNames;

                    if (returnedProps.containsKey(objCon.getObj().getJpaId()))
                    {
                        returnedPropNames =
                            returnedProps.get(objCon.getObj().getJpaId());
                    }
                    else
                    {
                        returnedPropNames = new ArrayList<String>();
                    }

                    for (DynamicProperty dp : objCon.getPropSet())
                    {
                        if (!returnedPropNames.contains(dp.getName()))
                        {
                            PropertyChange pc = new PropertyChange();
                            pc.setName(dp.getName());
                            pc.setVal(dp.getVal());
                            pc.setOp(PropertyChangeOp.Assign);
                            ou.getChangeSet().add(pc);

                            returnedPropNames.add(dp.getName());
                        }
                    }

                    returnedProps.put(
                        objCon.getObj().getJpaId(),
                        returnedPropNames);

                    pfu.getObjectSet().add(ou);
                }

            }
            catch (Exception e)
            {
                logger.error("unable to check for updates", e);
                throw new RemoteException("unable to check for updates");
            }

            us.getFilterSet().add(pfu);
        }

        return us;
    }

    private static String incrementVersion(String version)
    {
        return version == null || version.equals("") ? "1" : String
            .valueOf(Integer.valueOf(version) + 1);
    }

    @SuppressWarnings("unchecked")
    private static List<PropertyFilter> getPropertyFilters(String sessionKey)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        String qs =
            "FROM PropertyFilter WHERE Cae1300ef9c06f981a88944ae6969d51c='"
                + sessionKey + "'";
        Query q = session.createQuery(qs);

        List<PropertyFilter> sessionFilters = new ArrayList<PropertyFilter>();
        Iterator iter = q.iterate();

        while (iter.hasNext())
        {
            sessionFilters.add((PropertyFilter) iter.next());
        }

        return sessionFilters;
    }
}
