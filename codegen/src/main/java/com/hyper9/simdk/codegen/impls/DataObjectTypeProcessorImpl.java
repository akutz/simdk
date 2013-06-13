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

package com.hyper9.simdk.codegen.impls;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import com.hyper9.simdk.codegen.Constants;
import com.hyper9.simdk.codegen.types.DataObjectType;
import com.hyper9.simdk.codegen.types.MetaProperty;
import com.hyper9.simdk.codegen.types.OutType;

public class DataObjectTypeProcessorImpl extends
    TypeProcessorImpl<DataObjectType>
{
    @Override
    public void processClass(Class<?> clazz, String sourceFilePath)
        throws Exception
    {
        DataObjectType meta = new DataObjectTypeImpl(clazz, sourceFilePath);

        // Set the simple name name of the out type.
        meta.getOutType().setSimpleName(clazz.getSimpleName());

        // If the In class has a super class and it is not the Java Object then
        // we need to create a super class for the Out type.
        if (clazz.getSuperclass() != Object.class)
        {
            OutType outSuper = new OutTypeImpl();
            outSuper.setSimpleName(clazz.getSuperclass().getSimpleName());
            meta.getOutType().setSuperclass(outSuper);
        }

        // Enumerate this class's interfaces.
        for (Class<?> inter : clazz.getInterfaces())
        {
            OutType outInter = new OutTypeImpl();

            if (clazz == Constants.DYNAMIC_DATA_CLASS
                && inter == Serializable.class)
            {
                outInter.setSimpleName(inter.getSimpleName());
                outInter.setPackageName(inter.getPackage().getName());
                meta.getOutType().getInterfaces().add(outInter);
            }
            else if (clazz != Constants.DYNAMIC_DATA_CLASS
                && inter != Serializable.class)
            {
                outInter.setSimpleName(inter.getSimpleName());

                // If the package the interface belongs to is not a VMware
                // package the set the out type's package name.
                if (!inter.getPackage().getName().contains("vmware"))
                {
                    outInter.setPackageName(inter.getPackage().getName());
                }

                meta.getOutType().getInterfaces().add(outInter);
            }
        }

        // Introspect this class as if it were a bean.
        BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(clazz);

        // Get a list of the class's property descriptors.
        PropertyDescriptor[] propDescriptors =
            beanInfo.getPropertyDescriptors();

        // Define a list to hold all of this class's return types.
        for (PropertyDescriptor pd : propDescriptors)
        {
            // Get the property's read method, the getter.
            Method readMethod = pd.getReadMethod();

            // We want to skip any properties that are not explicitly defined
            // in this class, and we do this by detecting whether or not the
            // property's read method is declared in this class. There is some
            // room for false positives here, but it is not high with the
            // VI SDK.
            if (!readMethod.getDeclaringClass().equals(clazz))
            {
                continue;
            }

            // Create a meta property.
            MetaProperty mp = new MetaPropertyImpl(meta);

            // Set the property's name.
            mp.setName(pd.getName());

            // Set the property's out type information.
            String simpleName = pd.getPropertyType().getSimpleName();
            mp.getOutType().setSimpleName(simpleName);

            // If the package the property type belongs to is not a VMware
            // package then set the out type's name.
            if (pd.getPropertyType().getPackage() != null
                && !pd.getPropertyType().getPackage().getName().contains(
                    "vmware"))
            {
                mp.getOutType().setPackageName(
                    pd.getPropertyType().getPackage().getName());
            }

            // Add this property to the meta type's properties list.
            meta.getProperties().add(mp);
        }

        // Open the bean's source file and parse out the order of its
        // properties.
        FileReader fin = new FileReader(meta.getInTypeSourceFilePath());
        BufferedReader in = new BufferedReader(fin);

        int propOrder = 0;

        while (in.ready())
        {
            String line = in.readLine();

            Matcher m = Constants.FIELD_NAME_PATT.matcher(line);

            if (!m.matches())
            {
                continue;
            }

            String propName = m.group(1);

            for (MetaProperty mp : meta.getProperties())
            {
                if (mp.getName().equals(propName))
                {
                    mp.setOrder(propOrder);
                    break;
                }
            }

            ++propOrder;
        }

        in.close();

        getProcessedMetaTypes().put(meta.getOutType().getSimpleName(), meta);
    }

    @Override
    public boolean shouldProcess(Class<?> clazz, String sourceFilePath)
    {
        return Constants.DYNAMIC_DATA_CLASS.isAssignableFrom(clazz);
    }
}
