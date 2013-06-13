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

package com.hyper9.simdk.stubs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;

/**
 * A custom JAXB reader that allows for late-injection of transient annotations.
 * 
 * @author akutz
 * 
 */
@SuppressWarnings("unchecked")
public class TransientAnnotationReader extends
    AbstractInlineAnnotationReaderImpl<Type, Class, Field, Method> implements
    RuntimeAnnotationReader
{
    private static class XmlTransientProxyHandler implements InvocationHandler
    {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            if (args == null || args.length == 0)
            {
                if (method.getName().equals("annotationType"))
                {
                    return XmlTransient.class;
                }

                if (method.getName().equals("toString"))
                {
                    return "@XmlTransient";
                }
            }

            throw new UnsupportedOperationException(
                "@XmlTransient doesn't support method call: "
                    + method.getName());
        }

        private static XmlTransient create()
        {
            return (XmlTransient) Proxy.newProxyInstance(
                XmlTransientProxyHandler.class.getClassLoader(),
                new Class[]
                {
                    XmlTransient.class
                },
                new XmlTransientProxyHandler());
        }
    }

    private static final Annotation XML_TRANSIENT_ANNOTATION =
        XmlTransientProxyHandler.create();

    private static final Annotation[] XML_TRANSIENT_ANNOTATION_ONLY =
    {
        XML_TRANSIENT_ANNOTATION
    };

    private final RuntimeInlineAnnotationReader delegate =
        new RuntimeInlineAnnotationReader();
    private final List<Class<?>> transientClasses = new ArrayList<Class<?>>();
    private final List<Field> transientFields = new ArrayList<Field>();
    private final List<Method> transientMethods = new ArrayList<Method>();

    public TransientAnnotationReader()
    {
        try
        {
            addTransientField(Throwable.class.getDeclaredField("stackTrace"));
            addTransientMethod(Throwable.class
                .getDeclaredMethod("getStackTrace"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    public void addTransientClass(Class cls)
    {
        transientClasses.add(cls);
    }

    public void addTransientField(Field field)
    {
        transientFields.add(field);
    }

    public void addTransientMethod(Method method)
    {
        transientMethods.add(method);
    }

    public boolean hasClassAnnotation(
        Class clazz,
        Class<? extends Annotation> annotationType)
    {
        if (transientClasses.contains(clazz))
        {
            return true;
        }

        return delegate.hasClassAnnotation(clazz, annotationType);
    }

    public <A extends Annotation> A getClassAnnotation(
        Class<A> annotationType,
        Class clazz,
        Locatable srcPos)
    {
        if (transientClasses.contains(clazz))
        {
            return (A) XML_TRANSIENT_ANNOTATION;
        }

        return delegate.getClassAnnotation(annotationType, clazz, srcPos);
    }

    public boolean hasFieldAnnotation(
        Class<? extends Annotation> annotationType,
        Field field)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientFields.contains(field))
            {
                return true;
            }
        }
        return delegate.hasFieldAnnotation(annotationType, field);
    }

    public <A extends Annotation> A getFieldAnnotation(
        Class<A> annotationType,
        Field field,
        Locatable srcPos)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientFields.contains(field))
            {
                return (A) XML_TRANSIENT_ANNOTATION;
            }
        }
        return delegate.getFieldAnnotation(annotationType, field, srcPos);
    }

    public Annotation[] getAllFieldAnnotations(Field field, Locatable srcPos)
    {
        if (transientFields.contains(field))
        {
            return XML_TRANSIENT_ANNOTATION_ONLY;
        }

        return delegate.getAllFieldAnnotations(field, srcPos);
    }

    public boolean hasMethodAnnotation(
        Class<? extends Annotation> annotationType,
        Method method)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientMethods.contains(method))
            {
                return true;
            }
        }

        return delegate.hasMethodAnnotation(annotationType, method);
    }

    public <A extends Annotation> A getMethodAnnotation(
        Class<A> annotationType,
        Method method,
        Locatable srcPos)
    {
        if (XmlTransient.class.isAssignableFrom(annotationType))
        {
            if (transientMethods.contains(method))
            {
                return (A) XML_TRANSIENT_ANNOTATION;
            }
        }

        return delegate.getMethodAnnotation(annotationType, method, srcPos);
    }

    public Annotation[] getAllMethodAnnotations(Method method, Locatable srcPos)
    {
        if (transientMethods.contains(method))
        {
            return XML_TRANSIENT_ANNOTATION_ONLY;
        }

        return delegate.getAllMethodAnnotations(method, srcPos);
    }

    public <A extends Annotation> A getMethodParameterAnnotation(
        Class<A> annotation,
        Method method,
        int paramIndex,
        Locatable srcPos)
    {
        return delegate.getMethodParameterAnnotation(
            annotation,
            method,
            paramIndex,
            srcPos);
    }

    public <A extends Annotation> A getPackageAnnotation(
        Class<A> a,
        Class clazz,
        Locatable srcPos)
    {
        return delegate.getPackageAnnotation(a, clazz, srcPos);
    }

    public Class getClassValue(Annotation a, String name)
    {
        return (Class) delegate.getClassValue(a, name);
    }

    public Class[] getClassArrayValue(Annotation a, String name)
    {
        return (Class[]) delegate.getClassArrayValue(a, name);
    }

    @Override
    protected String fullName(Method m)
    {
        return m.getDeclaringClass().getName() + '#' + m.getName();
    }
}
