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

import java.lang.reflect.Method;
import com.hyper9.simdk.codegen.Constants;
import com.hyper9.simdk.codegen.types.ManagedObjectType;
import com.hyper9.simdk.codegen.types.MetaProperty;
import com.hyper9.simdk.codegen.types.OutType;

public class ManagedObjectTypeProcessorImpl extends
    TypeProcessorImpl<ManagedObjectType>
{

    @Override
    public void processClass(Class<?> clazz, String sourceFilePath)
        throws Exception
    {
        ManagedObjectType meta =
            new ManagedObjectTypeImpl(clazz, sourceFilePath);

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

        if (clazz != Constants.MANAGED_OBJECT_CLASS)
        {
            // Get the class's methods.
            Method[] methods = clazz.getMethods();

            for (Method m : methods)
            {
                if (m.getDeclaringClass() != clazz)
                {
                    continue;
                }

                if (!m.getName().matches("^(?:get|is).+$"))
                {
                    continue;
                }

                if (m.getParameterTypes() != null
                    && m.getParameterTypes().length > 0)
                {
                    continue;
                }

                String methodNameMinusPropertyVerb =
                    m.getName().startsWith("get") ? m.getName().substring(3)
                        : m.getName().substring(2);
                    
                String propName = firstCharToLowerCase(methodNameMinusPropertyVerb);

                MetaProperty mp = new MetaPropertyImpl(meta);
                mp.setName(propName);

                // Set the property's out type information.
                String simpleName = m.getReturnType().getSimpleName();
                mp.getOutType().setSimpleName(simpleName);
           
                // If the package the property type belongs to is not a VMware
                // package then set the out type's name.
                if (m.getReturnType().getPackage() != null
                    && !m.getReturnType().getPackage().getName().contains(
                        "vmware"))
                {
                    mp.getOutType().setPackageName(
                        m.getReturnType().getPackage().getName());
                }

                meta.getProperties().add(mp);
            }
        }

        getProcessedMetaTypes().put(meta.getOutType().getSimpleName(), meta);
    }

    @Override
    public boolean shouldProcess(Class<?> clazz, String sourceFilePath)
        throws Exception
    {
        return Constants.MANAGED_OBJECT_CLASS.isAssignableFrom(clazz);
    }

    private static String firstCharToLowerCase(String s)
    {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
}
