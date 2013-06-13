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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.reflect.Method;
import javax.persistence.Column;
import com.hyper9.simdk.stubs.GenericType;

public class TypeUtil
{
    public static BeanInfo getBeanInfo(Object object) throws Exception
    {
        //Class<?> realClass = getRealClass(object.getClass());
        BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass() /*realClass*/);
        return beanInfo;
    }

    public static Class<?> getRealClass(Class<?> proxyClass) throws Exception
    {
        if (!proxyClass.getName().matches("[^_]*?_\\$\\$_javassist_\\d+"))
        {
            return proxyClass;
        }

        String clazzName = proxyClass.getName();
        clazzName = clazzName.replaceAll("_\\$\\$_javassist_\\d+", "");
        Class<?> clazz = Class.forName(clazzName);
        return clazz;
    }

    public static Class<?> getGenericListType(Method method) throws Exception
    {
        Class<?> realClass = getRealClass(method.getDeclaringClass());

        try
        {
            method = realClass.getMethod(method.getName(), new Class<?>[0]);
        }
        catch (Exception e)
        {
            // Do nothing
        }

        GenericType gt = method.getAnnotation(GenericType.class);

        return gt.value();
    }

    /**
     * Gets the column name the given property maps to.
     * 
     * @param clazz The class the property belongs to.
     * @param propertyName The name of the property.
     * @return The column name the given property maps to.
     */
    public static String getColumnName(Class<?> clazz, String propertyName)
    {
        try
        {
            Method m =
                clazz.getMethod(
                    "get" + upCaseFirstChar(propertyName),
                    new Class<?>[0]);

            Column c = m.getAnnotation(Column.class);
            return c.name();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String upCaseFirstChar(String toUpCase)
    {
        return toUpCase.substring(0, 1).toUpperCase() + toUpCase.substring(1);
    }
}
