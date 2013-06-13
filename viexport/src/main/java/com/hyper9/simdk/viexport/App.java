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

package com.hyper9.simdk.viexport;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.hibernate.Session;
import org.w3c.dom.Element;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.stubs.ArrayOf;
import com.hyper9.simdk.stubs.ServiceConnection;
import com.hyper9.simdk.stubs.dao.DynamicData;
import com.hyper9.simdk.stubs.dao.DynamicProperty;
import com.hyper9.simdk.stubs.dao.ObjectContent;
import com.hyper9.simdk.stubs.dao.ObjectSpec;
import com.hyper9.simdk.stubs.dao.PropertyFilterSpec;
import com.hyper9.simdk.stubs.dao.PropertySpec;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;

/**
 * The application's entry point.
 * 
 * @author akutz
 * 
 */
public class App
{
    /**
     * A list of ManagedObjectReference IDs that have been processed.
     */
    private static List<String> morefIDsProcessed = new ArrayList<String>();

    /**
     * A list of the ManagedObjectReferences that have been dereferenced.
     */
    private static HashMap<String, ManagedObjectReference> morefsDereffed =
        new HashMap<String, ManagedObjectReference>();

    /**
     * The VI service connection.
     */
    private static ServiceConnection serviceConnection;

    /**
     * The command-line options.
     */
    private static Options cliOptions = new Options();

    /**
     * The VI server to connect to.
     */
    private static String viServer;

    /**
     * The port to the VI server on.
     */
    private static Integer viPort;

    /**
     * A flag indicating whether or not to use SSL when connecting to the VI
     * server.
     */
    private static boolean viUseSsl = false;

    /**
     * The user name to use when connecting to the VI server.
     */
    private static String viUserName;

    /**
     * The password to use when connecting to the VI server.
     */
    private static String viPassword;

    /**
     * The path to the Hibernate configuration file if persistence is desired.
     */
    private static String hibernateConfig = null;

    /**
     * Whether or not to persist the collected data using Hibernate.
     */
    private static boolean persist = false;

    /**
     * The application's entry point.
     * 
     * @param args Command line options and their arguments.
     * @throws Exception When an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
        // Set up the CLI options.
        setupCLIOpts();

        // Process the CLI options and arguments.
        if (!processCLIOpts(args))
        {
            return;
        }

        serviceConnection =
            ServiceConnection.open(
                viServer,
                viPort,
                viUserName,
                viPassword,
                null,
                viUseSsl,
                true);

        // Retrieve the service instance's properties.
        processObject(serviceConnection.getServiceInstance());

        // Logout.
        serviceConnection.getVimService().logout(
            serviceConnection.getServiceContent().getSessionManager());

        if (persist)
        {
            persist();
        }
    }

    private static void persist() throws Exception
    {
        if (hibernateConfig != null)
        {
            HibernateUtil.setConfigFilePath(hibernateConfig);
            System.out.println();
            System.out.println("using "
                + HibernateUtil.getConfigFile().getAbsolutePath()
                + " to persist data");
            System.out.println();
        }

        // Get the current session and begin a transaction.
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        session.save(serviceConnection.getServiceInstance());

        if (serviceConnection.getInternalServiceContent() != null)
        {
            session.save(serviceConnection.getInternalServiceContent());
        }

        session.getTransaction().commit();
    }

    @SuppressWarnings("static-access")
    private static void setupCLIOpts()
    {
        Option serverOption =
            OptionBuilder
                .withLongOpt("server")
                .withDescription("the vi server to connect to")
                .isRequired(true)
                .hasArg()
                .withArgName("arg")
                .create("s");
        cliOptions.addOption(serverOption);

        Option userNameOption =
            OptionBuilder
                .withLongOpt("userName")
                .withDescription("the user name to connect with")
                .isRequired(true)
                .hasArg()
                .withArgName("arg")
                .create("u");
        cliOptions.addOption(userNameOption);

        Option passwordOption =
            OptionBuilder
                .withLongOpt("pass")
                .withDescription("the password to connect with")
                .isRequired(true)
                .hasArg()
                .withArgName("arg")
                .create("p");
        cliOptions.addOption(passwordOption);

        Option useSslOption =
            OptionBuilder
                .withLongOpt("useSsl")
                .withDescription("a flag indicating whether or not to use ssl")
                .isRequired(false)
                .hasArg(false)
                .create("l");
        cliOptions.addOption(useSslOption);

        Option portOption =
            OptionBuilder
                .withLongOpt("port")
                .withDescription(
                    "the port to connect to the server on (defaults to 80 for non-SSL and 443 for SSL)")
                .isRequired(false)
                .hasArg(true)
                .withArgName("arg")
                .create("t");
        cliOptions.addOption(portOption);

        Option persistOption =
            OptionBuilder
                .withLongOpt("persist")
                .withDescription(
                    "whether or not to persist the collected data using hibernate")
                .isRequired(false)
                .hasArg(false)
                .create("e");
        cliOptions.addOption(persistOption);

        Option hibernateConfigOption =
            OptionBuilder
                .withLongOpt("hibernateConfig")
                .withDescription(
                    "the path to a hibernate configuration file used to persist the collected data")
                .isRequired(false)
                .hasArg(true)
                .withArgName("arg")
                .create("h");
        cliOptions.addOption(hibernateConfigOption);
    }

    private static boolean processCLIOpts(String[] args)
    {
        try
        {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(cliOptions, args);

            viServer = line.getOptionValue("s");
            viUserName = line.getOptionValue("u");
            viPassword = line.getOptionValue("p");
            viUseSsl = line.hasOption("l");

            if (line.hasOption("t"))
            {
                viPort = Integer.parseInt(line.getOptionValue("t"));
            }

            if (line.hasOption("h"))
            {
                hibernateConfig = line.getOptionValue("h");
            }

            persist = line.hasOption("e");

            return true;
        }
        catch (ParseException exp)
        {
            System.err.println();
            System.err.println(exp.getMessage());
            printUsageAndHelp();
            return false;
        }
    }

    private static void printUsageAndHelp()
    {
        System.out.println();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("viexport", cliOptions, true);
        System.out.println();
    }

    /**
     * Processes an object's properties.
     * 
     * @param toProcess The object to process.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unchecked")
    private static void processObject(Object toProcess) throws Exception
    {
        if (toProcess == null)
        {
            return;
        }

        if (toProcess instanceof List)
        {
            processObject((List) toProcess);
        }
        else if (toProcess instanceof ManagedObjectReference)
        {
            processObject((ManagedObjectReference) toProcess);
        }
        else if (toProcess instanceof DynamicData)
        {
            processObject((DynamicData) toProcess);
        }
    }

    @SuppressWarnings("unchecked")
    private static void processObject(List list) throws Exception
    {
        for (Object o : list)
        {
            processObject(o);
        }
    }

    private static void processObject(DynamicData toRetrieve) throws Exception
    {
        processProperties(toRetrieve);
    }

    private static void processObject(ManagedObjectReference toRetrieve)
        throws Exception
    {
        if (morefIDsProcessed.contains(toRetrieve.get_Value()))
        {
            System.out.println("moref has been processed: type="
                + toRetrieve.getType() + ", value=" + toRetrieve.get_Value());
            return;
        }

        morefIDsProcessed.add(toRetrieve.get_Value());

        // Retrieve the ManagedObjectReference's properties.
        retrieveProperties(toRetrieve);

        // Process the ManagedObjectReference's properties.
        processProperties(toRetrieve);
    }

    private static void processProperties(Object toProcess) throws Exception
    {
        BeanInfo beanInfo = Introspector.getBeanInfo(toProcess.getClass());
        PropertyDescriptor[] pdArr = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor pd : pdArr)
        {
            Object val = pd.getReadMethod().invoke(toProcess, new Object[0]);

            // Dereference the value if it is a ManagedObjectReference or a list
            // of ManagedObjectReferences.
            val = derefMoRef(val);

            if (pd.getWriteMethod() != null && val != null)
            {
                if (val != null && val instanceof String
                    && ((String) val).length() > 254)
                {
                    val = ((String) val).substring(0, 254);
                }

                pd.getWriteMethod().invoke(toProcess, val);
            }

            processObject(val);
        }
    }

    @SuppressWarnings("unchecked")
    private static void retrieveProperties(ManagedObjectReference toRetrieve)
        throws Exception
    {
        PropertySpec pspec = new PropertySpec();
        pspec.setAll(true);
        pspec.setType(toRetrieve.getType());

        ObjectSpec ospec = new ObjectSpec();
        ospec.setObj(toRetrieve);
        ospec.setSkip(false);

        PropertyFilterSpec pfspec = new PropertyFilterSpec();
        pfspec.getObjectSet().add(ospec);
        pfspec.getPropSet().add(pspec);

        List<PropertyFilterSpec> pfspecList =
            new ArrayList<PropertyFilterSpec>();
        pfspecList.add(pfspec);

        List<ObjectContent> objConList = null;

        try
        {
            objConList =
                serviceConnection.getVimService().retrieveProperties(
                    serviceConnection
                        .getServiceContent()
                        .getPropertyCollector(),
                    pfspecList);
        }
        catch (Exception e)
        {
            // e.printStackTrace(System.err);
        }

        if (objConList == null)
        {
            return;
        }

        for (ObjectContent objCon : objConList)
        {
            List<DynamicProperty> dynProps = objCon.getPropSet();

            if (dynProps == null)
            {
                continue;
            }

            BeanInfo beanInfo = Introspector.getBeanInfo(toRetrieve.getClass());
            PropertyDescriptor[] pdArr = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor pd : pdArr)
            {
                DynamicProperty theDP = null;

                for (DynamicProperty dp : dynProps)
                {
                    if (dp.getName().equals(pd.getName()))
                    {
                        theDP = dp;
                        break;
                    }
                }

                if (theDP == null)
                {
                    continue;
                }

                Object val = theDP.getVal();

                if (val == null)
                {
                    continue;
                }

                if (val instanceof XMLGregorianCalendar)
                {
                    val = ((XMLGregorianCalendar) val).toGregorianCalendar();
                }
                else if (val instanceof ArrayOf)
                {
                    val = ((ArrayOf) val).getData();
                }

                val = derefMoRef(val);

                try
                {
                    pd.getWriteMethod().invoke(toRetrieve, val);
                }
                catch (Exception e)
                {
                    Element el = (Element) val;

                    String actualType =
                        el.getAttributes().item(1).getNodeValue();

                    System.err.println("unable to parse out a " + actualType);

                    throw e;
                }
            }
        }
    }

    /**
     * Processes a property's value before it is sent through a retrieval
     * process. This ensures that ManagedObjectReferences are dereferenced and
     * that lists are recursed into and their values processed as well.
     * 
     * @param toProcess The property value to process.
     * @return The processed property value.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unchecked")
    private static Object derefMoRef(Object toProcess) throws Exception
    {
        Object processed = toProcess;

        if (toProcess instanceof ManagedObjectReference)
        {
            processed = derefMoRef((ManagedObjectReference) toProcess);
        }
        else if (toProcess instanceof List)
        {
            List valAsList = (List) toProcess;

            for (int x = 0; x < valAsList.size(); ++x)
            {
                valAsList.set(x, derefMoRef(valAsList.get(x)));
            }

            processed = valAsList;
        }

        return processed;
    }

    /**
     * This method infers the ManagedObjectReference's class type from the type
     * property and returns a new instance of the dereferenced ManagedObject
     * type.
     * 
     * @param toDeref The ManagedObjectReference to dereference.
     * @return A new instance of the dereferenced ManagedObject type.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unchecked")
    private static ManagedObjectReference derefMoRef(
        ManagedObjectReference toDeref) throws Exception
    {
        // Check to see if this ManagedObjectReference has already been
        // dereferenced.
        if (morefsDereffed.containsKey(toDeref.get_Value()))
        {
            return morefsDereffed.get(toDeref.get_Value());
        }

        // Build the name of the dereferenced ManagedObject class type.
        String derefedClassTypeName =
            String.format("%s.%s", ManagedObjectReference.class
                .getPackage()
                .getName(), toDeref.getType());

        // Get the class from the system class loader.
        Class<? extends ManagedObjectReference> morefClazz =
            (Class<? extends ManagedObjectReference>) Class
                .forName(derefedClassTypeName);

        // Get the class's constructor.
        Constructor<? extends ManagedObjectReference> morefCtor =
            morefClazz.getConstructor(new Class<?>[0]);

        // Create a new instance of the dereferenced ManagedObject.
        ManagedObjectReference dereffedMoref =
            morefCtor.newInstance(new Object[0]);

        System.out.println("constructed a "
            + dereffedMoref.getClass().getName());

        // Copy the properties of the ManagedObjectReference to the new
        // dereferenced object.
        dereffedMoref.set_Value(toDeref.get_Value());
        dereffedMoref.setType(toDeref.getType());
        dereffedMoref.setServerGuid(toDeref.getServerGuid());

        // Note that this ManagedObjectReference has been dereferenced.
        morefsDereffed.put(dereffedMoref.get_Value(), dereffedMoref);

        return dereffedMoref;
    }
}
