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

package com.hyper9.simdk.ws;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.LogManager;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import com.hyper9.simdk.ws.util.ConfigFileUtil;
import com.hyper9.simdk.stubs.VimServiceImpl;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.db.impls.SettingImpl;
import com.hyper9.simdk.db.types.Setting;

/**
 * The servlet responsible for initializing the application.
 * 
 * @author akutz
 * 
 */
public class InitApplicationServlet extends HttpServlet implements Servlet
{
    /**
     * The generated serial version UID.
     */
    private static final long serialVersionUID = -2352851815204149534L;

    private ServletContext cxt;
    private static Properties props = new Properties();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        this.cxt = getServletContext();

        // Initialize JAAS.
        initJaas();

        // Load the properties file.
        try
        {
            loadPropertiesFile();
        }
        catch (Exception e)
        {
            this.cxt.log("error initializing properties", e);
        }

        // Initialize the database.
        try
        {
            DBInitializer.init();
        }
        catch (Exception e)
        {
            this.cxt.log("error initializing database", e);
        }

        // Initialize the VimInventory
        try
        {
            VimServiceImpl.setVimServiceExClass(VimServiceExImpl.class);
        }
        catch (Exception e)
        {
            this.cxt.log("error initializing vim inventory", e);
        }

        initSysProps();
    }

    @SuppressWarnings("unchecked")
    private void initSysProps()
    {
        // Read in the system properties from the database.
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        // Begin a new transaction.
        session.beginTransaction();

        // Build a query that will retrieve all of the settings.
        Query allSysPropsQuery =
            session.createQuery("FROM " + SettingImpl.class.getSimpleName());

        // Get an iterator for the database settings results.
        Iterator allSysPropsQueryIter = allSysPropsQuery.iterate();

        // Read in the system properties.
        while (allSysPropsQueryIter.hasNext())
        {
            Setting setting = (Setting) allSysPropsQueryIter.next();
            props.put(setting.getKey(), setting.getValue());
        }

        // Override any of the system properties in the database with
        // any system properties of the same name that were passed to the JVM.
        for (Object k : props.keySet())
        {
            if (System.getProperties().contains(k))
            {
                props.put(k, System.getProperties().get(k));
            }
        }

        // Commit the current transaction.
        session.getTransaction().commit();
    }

    private void initJaas()
    {
        String path =
            ConfigFileUtil.getFile("security.config").getAbsolutePath();
        System.getProperties().put("java.security.auth.login.config", path);
    }

    private void loadPropertiesFile()
    {
        File f = ConfigFileUtil.getFile("ws.properties");

        this.cxt.log("configuring log4j logging");
        PropertyConfigurator.configure(f.getPath());

        this.cxt.log("configuring java logging");
        try
        {
            LogManager lm = java.util.logging.LogManager.getLogManager();
            FileInputStream vmmPropsFileFIS = new FileInputStream(f);
            lm.readConfiguration(vmmPropsFileFIS);
        }
        catch (final Exception e)
        {
            this.cxt.log(String.format(
                "error configuring java logging; msg=%s",
                e.getMessage()));
        }

        try
        {
            this.cxt.log("loading properties");
            props.load(new FileInputStream(f.getPath()));
        }
        catch (final Exception e)
        {
            this.cxt.log("error loading properties", e);
        }
    }

    /**
     * Gets the configured properties.
     * 
     * @return The configured properties.
     */
    public static Properties getProperties()
    {
        return props;
    }
}
