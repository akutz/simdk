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

package com.hyper9.simdk.db;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import com.hyper9.simdk.db.impls.RoleImpl;
import com.hyper9.simdk.db.impls.SettingImpl;
import com.hyper9.simdk.db.impls.UserImpl;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;
import com.hyper9.simdk.stubs.SerializableObjectWrapper;
import com.hyper9.simdk.stubs.dao.DynamicProperty;
import com.hyper9.simdk.stubs.faults.MethodFault;

/**
 * A utility class for working with Hibernate.
 * 
 * @author akutz
 * 
 */
public class HibernateUtil
{
    private static Pattern PACKAGE_NAME_PATT =
        Pattern.compile("(.*?)\\.[^\\.]+");

    private static String MY_PACKAGE_NAME =
        HibernateUtil.class.getPackage().getName();

    private static SessionFactory sessionFactory;
    private static File configFile;

    public static File getConfigFile()
    {
        return configFile;
    }

    public static void setConfigFilePath(String toSet)
    {
        configFile = new File(toSet);
    }

    private static void buildSessionFactory()
    {
        try
        {
            AnnotationConfiguration ac = new AnnotationConfiguration();

            ac.addAnnotatedClass(DynamicProperty.class);
            ac.addAnnotatedClass(ManagedObjectReference.class);
            ac.addAnnotatedClass(SerializableObjectWrapper.class);
            ac.addAnnotatedClass(MethodFault.class);
            ac.addAnnotatedClass(SettingImpl.class);
            ac.addAnnotatedClass(UserImpl.class);
            ac.addAnnotatedClass(RoleImpl.class);

            // Add all of the classes from the DataObjects package.
            for (Class<?> c : com.hyper9.simdk.stubs.dao.AllClasses.getAllClasses())
            {
                ac.addAnnotatedClass(c);
            }

            // Add all of the classes from the ManagedObjects package.
            for (Class<?> c : com.hyper9.simdk.stubs.mao.AllClasses.getAllClasses())
            {
                if (c.getSimpleName().matches(
                    "(?:Extensible)?Managed(?:Object)"))
                {
                    continue;
                }

                ac.addAnnotatedClass(c);
            }

            if (configFile == null)
            {
                String callerPackageSlash =
                    getCallingPackageName().replace('.', '/');
                String pathToConfig =
                    String.format("%s/hibernate.cfg.xml", callerPackageSlash);
                sessionFactory =
                    ac.configure(pathToConfig).buildSessionFactory();
            }
            else
            {
                sessionFactory = ac.configure(configFile).buildSessionFactory();
            }
        }
        catch (Throwable e)
        {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        if (sessionFactory == null)
        {
            buildSessionFactory();
        }

        return sessionFactory;
    }

    private static String getCallingPackageName()
    {
        Throwable t = new Throwable();
        StackTraceElement[] stacks = t.getStackTrace();

        for (StackTraceElement el : stacks)
        {
            String elClassName = el.getClassName();
            
            Matcher m = PACKAGE_NAME_PATT.matcher(elClassName);
            
            if (!m.matches())
            {
                continue;
            }
            
            String name = m.group(1);

            if (!name.equals(MY_PACKAGE_NAME))
            {
                return name;
            }
        }

        return MY_PACKAGE_NAME;
    }
}
