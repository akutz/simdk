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

package com.hyper9.simdk.ws;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import junit.framework.Assert;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.hyper9.simdk.ws.DBInitializer;
import com.hyper9.simdk.ws.VimServiceExImpl;
import com.hyper9.simdk.ws.util.ServiceInstanceUtil;
import com.hyper9.simdk.stubs.TransientAnnotationReader;
import com.hyper9.simdk.stubs.dao.ObjectContent;
import com.hyper9.simdk.stubs.dao.ObjectSpec;
import com.hyper9.simdk.stubs.dao.PropertyFilterSpec;
import com.hyper9.simdk.stubs.dao.PropertySpec;
import com.hyper9.simdk.stubs.dao.SelectionSpec;
import com.hyper9.simdk.stubs.dao.TraversalSpec;
import com.hyper9.simdk.stubs.dao.UpdateSet;
import com.hyper9.simdk.stubs.dao.UserSession;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.stubs.jaxws.CheckForUpdatesResponse;
import com.hyper9.simdk.stubs.jaxws.CreateContainerViewResponse;
import com.hyper9.simdk.stubs.jaxws.CreateFilterResponse;
import com.hyper9.simdk.stubs.jaxws.CreateListViewResponse;
import com.hyper9.simdk.stubs.jaxws.LoginResponse;
import com.hyper9.simdk.stubs.jaxws.LogoutResponse;
import com.hyper9.simdk.stubs.jaxws.RetrievePropertiesResponse;
import com.hyper9.simdk.stubs.mao.ContainerView;
import com.hyper9.simdk.stubs.mao.ListView;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;
import com.hyper9.simdk.stubs.mao.PropertyFilter;
import com.hyper9.simdk.stubs.mao.ServiceInstance;
import com.sun.xml.bind.api.JAXBRIContext;

/**
 * The main test class for this project.
 * 
 * @author akutz
 * 
 */
public class VimServiceExImplTest
{
    private static VimServiceExImpl vimService = new VimServiceExImpl();
    private static JAXBContext jaxbContent;

    @BeforeClass
    public static void beforeClass()
    {
        try
        {
            // Indicate to the VimService that we are unit testing.
            vimService.setUnitTesting(true);

            // Get the path to the properties file.
            String path = "src/main/webapp/conf/ws.properties";
            File f = new File(path);
            PropertyConfigurator.configure(f.getPath());

            // Set up the JAAS configuration file.
            System.getProperties().put(
                "java.security.auth.login.config",
                "src/main/webapp/conf/security.config");

            // Initialize the database.
            DBInitializer.init();

            // Create the TransientAnnotationReader that marks the stackTrace
            // property of the Throwable interface as XmlTransient.
            TransientAnnotationReader reader = new TransientAnnotationReader();

            Map<String, Object> jaxbConfig = new HashMap<String, Object>();
            jaxbConfig.put(JAXBRIContext.ANNOTATION_READER, reader);

            // Create a new JAX-B context for this class's unit tests.
            jaxbContent =
                JAXBContext.newInstance(
                    String.format(
                        "%s:%s:%s",
                        com.hyper9.simdk.stubs.jaxws.AbortBackup.class
                            .getPackage()
                            .getName(),
                        com.hyper9.simdk.stubs.dao.AboutInfo.class
                            .getPackage()
                            .getName(),
                        com.hyper9.simdk.stubs.mao.AgentManager.class
                            .getPackage()
                            .getName()),
                    reader.getClass().getClassLoader(),
                    jaxbConfig);

            HibernateUtil
                .getSessionFactory()
                .getCurrentSession()
                .beginTransaction();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    @AfterClass
    public static void afterClass()
    {
        HibernateUtil
            .getSessionFactory()
            .getCurrentSession()
            .getTransaction()
            .commit();
    }

    @Before
    public void beforeTest()
    {

    }

    @After
    public void afterTest()
    {

    }

    private String marshall(Object toMarshall) throws Exception
    {
        Marshaller marshaller = jaxbContent.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        marshaller.marshal(toMarshall, sw);
        return sw.toString();
    }

    @Test
    public void testLogin() throws Exception
    {
        UserSession us =
            vimService.login(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getSessionManager(), "root", "password", null);

        Assert.assertNotNull(us);
        Assert.assertEquals("root", us.getUserName());

        LoginResponse response = new LoginResponse();
        response.setReturnval(us);
        System.out.println(marshall(response));
    }

    @Test
    public void testLogout() throws Exception
    {
        vimService.logout(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getSessionManager());

        LogoutResponse response = new LogoutResponse();
        System.out.println(marshall(response));
    }

    @Test
    public void testCreateContainerView() throws Exception
    {
        ManagedObjectReference rootFolder = new ManagedObjectReference();
        rootFolder.setType("Folder");
        rootFolder.set_Value("group-d1");

        ContainerView containerView =
            (ContainerView) vimService.createContainerView(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getViewManager(), rootFolder, null, true);

        CreateContainerViewResponse response =
            new CreateContainerViewResponse();
        response.setReturnval(containerView);
        System.out.println(marshall(response));
    }

    @Test
    public void testCreateListView() throws Exception
    {
        List<ManagedObjectReference> toAdd =
            new ArrayList<ManagedObjectReference>();
        toAdd.add(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getAuthorizationManager());
        toAdd.add(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getPerfManager());

        ListView listView =
            (ListView) vimService.createListView(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getViewManager(), toAdd);

        CreateListViewResponse response = new CreateListViewResponse();
        response.setReturnval(listView);
        System.out.println(marshall(response));
    }

    @Test
    public void testCreateFilter() throws Exception
    {
        PropertyFilterSpec propFilterSpec = new PropertyFilterSpec();

        PropertyFilter propFilter =
            (PropertyFilter) vimService.createFilter(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getPropertyCollector(), propFilterSpec, false);

        CreateFilterResponse response = new CreateFilterResponse();
        response.setReturnval(propFilter);
        System.out.println(marshall(response));
    }

    @Test
    public void testRetrieveProperties() throws Exception
    {
        ObjectSpec objSpec1 = new ObjectSpec();
        objSpec1.setObj(ServiceInstanceUtil.getServiceInstance());

        ObjectSpec objSpec2 = new ObjectSpec();
        objSpec2.setObj(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getAuthorizationManager());

        PropertySpec propSpec1 = new PropertySpec();
        propSpec1.setType(objSpec1.getObj().getType());
        propSpec1.getPathSet().add("capability.multiHostSupported");
        propSpec1.getPathSet().add("content.about.fullName");

        PropertySpec propSpec2 = new PropertySpec();
        propSpec2.setType(objSpec2.getObj().getType());
        propSpec2.setAll(true);

        PropertyFilterSpec propFilterSpec = new PropertyFilterSpec();
        propFilterSpec.getObjectSet().add(objSpec1);
        propFilterSpec.getObjectSet().add(objSpec2);
        propFilterSpec.getPropSet().add(propSpec1);
        propFilterSpec.getPropSet().add(propSpec2);

        List<PropertyFilterSpec> pfsList = new ArrayList<PropertyFilterSpec>();
        pfsList.add(propFilterSpec);

        List<ObjectContent> objContList =
            vimService.retrieveProperties(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getPropertyCollector(), pfsList);

        RetrievePropertiesResponse response = new RetrievePropertiesResponse();
        response.setReturnval(objContList);
        System.out.println(marshall(response));
    }

    @Test
    public void testRetrievePropertiesTraversalSpec() throws Exception
    {
        TraversalSpec si2pm = new TraversalSpec();
        si2pm.setPath("content.perfManager");
        si2pm.setType(ServiceInstance.class.getSimpleName());

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(ServiceInstanceUtil.getServiceInstance());
        objSpec.getSelectSet().add(si2pm);

        PropertySpec propSpec1 = new PropertySpec();
        propSpec1.setType(objSpec.getObj().getType());
        propSpec1.getPathSet().add("capability.multiHostSupported");
        propSpec1.getPathSet().add("content.about.fullName");

        PropertySpec propSpec2 = new PropertySpec();
        propSpec2.setType("PerformanceManager");
        propSpec2.getPathSet().add("perfCounter");

        PropertyFilterSpec propFilterSpec = new PropertyFilterSpec();
        propFilterSpec.getObjectSet().add(objSpec);
        propFilterSpec.getPropSet().add(propSpec1);
        propFilterSpec.getPropSet().add(propSpec2);

        List<PropertyFilterSpec> pfsList = new ArrayList<PropertyFilterSpec>();
        pfsList.add(propFilterSpec);

        List<ObjectContent> objContList =
            vimService.retrieveProperties(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getPropertyCollector(), pfsList);

        RetrievePropertiesResponse response = new RetrievePropertiesResponse();
        response.setReturnval(objContList);
        System.out.println(marshall(response));
    }

    @Test
    public void testRetrievePropertiesArrayOfString() throws Exception
    {
        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getUserDirectory());

        PropertySpec propSpec1 = new PropertySpec();
        propSpec1.setType(objSpec.getObj().getType());
        propSpec1.getPathSet().add("domainList");

        PropertyFilterSpec propFilterSpec = new PropertyFilterSpec();
        propFilterSpec.getObjectSet().add(objSpec);
        propFilterSpec.getPropSet().add(propSpec1);

        List<PropertyFilterSpec> pfsList = new ArrayList<PropertyFilterSpec>();
        pfsList.add(propFilterSpec);

        List<ObjectContent> objContList =
            vimService.retrieveProperties(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getPropertyCollector(), pfsList);

        RetrievePropertiesResponse response = new RetrievePropertiesResponse();
        response.setReturnval(objContList);
        System.out.println(marshall(response));
    }

    @Test
    public void testRetrievePropertiesWithTraveralSpecs() throws Exception
    {
        SelectionSpec namedFolderTraversalSpec = new SelectionSpec();
        namedFolderTraversalSpec.setName("folderTraversalSpec");

        SelectionSpec namedDatacenterHostTraversalSpec = new SelectionSpec();
        namedDatacenterHostTraversalSpec.setName("datacenterHostTraversalSpec");

        SelectionSpec namedDatacenterVmTraversalSpec = new SelectionSpec();
        namedDatacenterVmTraversalSpec.setName("datacenterVmTraversalSpec");

        SelectionSpec namedComputeResourceRpTraversalSpec = new SelectionSpec();
        namedComputeResourceRpTraversalSpec
            .setName("computeResourceRpTraversalSpec");

        SelectionSpec namedComputeResourceHostTraversalSpec =
            new SelectionSpec();
        namedComputeResourceHostTraversalSpec
            .setName("computeResourceHostTraversalSpec");

        SelectionSpec namedHostVmTraversalSpec = new SelectionSpec();
        namedHostVmTraversalSpec.setName("hostVmTraversalSpec");

        SelectionSpec namedResourcePoolTraversalSpec = new SelectionSpec();
        namedResourcePoolTraversalSpec.setName("resourcePoolTraversalSpec");

        SelectionSpec namedResourcePoolVmTraversalSpec = new SelectionSpec();
        namedResourcePoolVmTraversalSpec.setName("resourcePoolVmTraversalSpec");

        TraversalSpec folderTraversalSpec = new TraversalSpec();
        folderTraversalSpec.setName("folderTraversalSpec");
        folderTraversalSpec.setType("Folder");
        folderTraversalSpec.setPath("childEntity");
        folderTraversalSpec.setSkip(false);

        TraversalSpec datacenterVmTraversalSpec = new TraversalSpec();
        datacenterVmTraversalSpec.setName("datacenterVmTraversalSpec");
        datacenterVmTraversalSpec.setType("Datacenter");
        datacenterVmTraversalSpec.setPath("vmFolder");
        datacenterVmTraversalSpec.setSkip(false);

        TraversalSpec datacenterHostTraversalSpec = new TraversalSpec();
        datacenterHostTraversalSpec.setName("datacenterHostTraversalSpec");
        datacenterHostTraversalSpec.setType("Datacenter");
        datacenterHostTraversalSpec.setPath("hostFolder");
        datacenterHostTraversalSpec.setSkip(false);

        TraversalSpec computeResourceHostTraversalSpec = new TraversalSpec();
        computeResourceHostTraversalSpec
            .setName("computeResourceHostTraversalSpec");
        computeResourceHostTraversalSpec.setType("ComputeResource");
        computeResourceHostTraversalSpec.setPath("host");
        computeResourceHostTraversalSpec.setSkip(false);

        TraversalSpec computeResourceRpTraversalSpec = new TraversalSpec();
        computeResourceRpTraversalSpec
            .setName("computeResourceRpTraversalSpec");
        computeResourceRpTraversalSpec.setType("ComputeResource");
        computeResourceRpTraversalSpec.setPath("resourcePool");
        computeResourceRpTraversalSpec.setSkip(false);

        TraversalSpec resourcePoolTraversalSpec = new TraversalSpec();
        resourcePoolTraversalSpec.setName("resourcePoolTraversalSpec");
        resourcePoolTraversalSpec.setType("ResourcePool");
        resourcePoolTraversalSpec.setPath("resourcePool");
        resourcePoolTraversalSpec.setSkip(false);

        TraversalSpec hostVmTraversalSpec = new TraversalSpec();
        hostVmTraversalSpec.setName("hostVmTraversalSpec");
        hostVmTraversalSpec.setType("HostSystem");
        hostVmTraversalSpec.setPath("vm");
        hostVmTraversalSpec.setSkip(false);

        TraversalSpec resourcePoolVmTraversalSpec = new TraversalSpec();
        resourcePoolVmTraversalSpec.setName("resourcePoolVmTraversalSpec");
        resourcePoolVmTraversalSpec.setType("ResourcePool");
        resourcePoolVmTraversalSpec.setPath("vm");
        resourcePoolVmTraversalSpec.setSkip(false);

        folderTraversalSpec.getSelectSet().add(namedFolderTraversalSpec);
        folderTraversalSpec
            .getSelectSet()
            .add(namedDatacenterHostTraversalSpec);
        folderTraversalSpec.getSelectSet().add(namedDatacenterVmTraversalSpec);
        folderTraversalSpec.getSelectSet().add(
            namedComputeResourceRpTraversalSpec);
        folderTraversalSpec.getSelectSet().add(
            namedComputeResourceHostTraversalSpec);
        folderTraversalSpec.getSelectSet().add(namedHostVmTraversalSpec);
        folderTraversalSpec
            .getSelectSet()
            .add(namedResourcePoolVmTraversalSpec);

        datacenterVmTraversalSpec.getSelectSet().add(namedFolderTraversalSpec);

        datacenterHostTraversalSpec
            .getSelectSet()
            .add(namedFolderTraversalSpec);

        computeResourceRpTraversalSpec.getSelectSet().add(
            namedResourcePoolTraversalSpec);
        computeResourceRpTraversalSpec.getSelectSet().add(
            namedResourcePoolVmTraversalSpec);

        resourcePoolTraversalSpec.getSelectSet().add(
            namedResourcePoolTraversalSpec);
        resourcePoolTraversalSpec.getSelectSet().add(
            namedResourcePoolVmTraversalSpec);

        hostVmTraversalSpec.getSelectSet().add(namedFolderTraversalSpec);

        PropertySpec pspec = new PropertySpec();
        pspec.setAll(false);
        pspec.setType("VirtualMachine");

        ManagedObjectReference obj = new ManagedObjectReference();
        obj.set_Value("group-d1");
        obj.setType("Folder");

        ObjectSpec ospec = new ObjectSpec();
        ospec.setObj(obj);
        ospec.setSkip(false);
        ospec.getSelectSet().add(folderTraversalSpec);
        ospec.getSelectSet().add(datacenterVmTraversalSpec);
        ospec.getSelectSet().add(datacenterHostTraversalSpec);
        ospec.getSelectSet().add(computeResourceHostTraversalSpec);
        ospec.getSelectSet().add(computeResourceRpTraversalSpec);
        ospec.getSelectSet().add(resourcePoolTraversalSpec);
        ospec.getSelectSet().add(hostVmTraversalSpec);
        ospec.getSelectSet().add(resourcePoolVmTraversalSpec);

        PropertyFilterSpec propFilter = new PropertyFilterSpec();
        propFilter.getObjectSet().add(ospec);
        propFilter.getPropSet().add(pspec);

        List<PropertyFilterSpec> propFilterList =
            new ArrayList<PropertyFilterSpec>();
        propFilterList.add(propFilter);

        ManagedObjectReference propColl = new ManagedObjectReference();
        propColl.set_Value("PropertyCollector");
        propColl.setType("PropertyCollector");

        List<ObjectContent> objContList =
            vimService.retrieveProperties(propColl, propFilterList);

        Assert.assertNotNull(objContList);
    }

    @Test
    public void testCheckForUpdates() throws Exception
    {
        ManagedObjectReference containerView =
            vimService.createContainerView(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getViewManager(), ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getRootFolder(), null, true);

        ManagedObjectReference containerViewMoref =
            new ManagedObjectReference();
        containerViewMoref.setType("ContainerView");
        containerViewMoref.set_Value(containerView.get_Value());

        ManagedObjectReference userDirectoryMoref =
            new ManagedObjectReference();
        userDirectoryMoref.setType("UserDirectory");
        userDirectoryMoref.set_Value("UserDirectory");

        List<ManagedObjectReference> listViewObj =
            new ArrayList<ManagedObjectReference>();
        listViewObj.add(containerViewMoref);
        listViewObj.add(userDirectoryMoref);

        ManagedObjectReference listView =
            vimService.createListView(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getViewManager(), listViewObj);

        ManagedObjectReference listViewMoref = new ManagedObjectReference();
        listViewMoref.setType("ListView");
        listViewMoref.set_Value(listView.get_Value());

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(listViewMoref);

        PropertySpec propSpec1 = new PropertySpec();
        propSpec1.setType(objSpec.getObj().getType());
        propSpec1.setAll(true);

        PropertyFilterSpec propFilterSpec = new PropertyFilterSpec();
        propFilterSpec.getObjectSet().add(objSpec);
        propFilterSpec.getPropSet().add(propSpec1);

        vimService.createFilter(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getPropertyCollector(), propFilterSpec, false);

        UpdateSet updateSet =
            vimService.checkForUpdates(ServiceInstanceUtil
                .getServiceInstance()
                .getContent()
                .getPropertyCollector(), null);

        CheckForUpdatesResponse response = new CheckForUpdatesResponse();
        response.setReturnval(updateSet);
        System.out.println(marshall(response));
    }
}
