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

package com.hyper9.simdk.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import com.hyper9.simdk.codegen.impls.DataObjectTypeProcessorImpl;
import com.hyper9.simdk.codegen.impls.DerefInfoImpl;
import com.hyper9.simdk.codegen.impls.EnumTypeProcessorImpl;
import com.hyper9.simdk.codegen.impls.FaultTypeProcessorImpl;
import com.hyper9.simdk.codegen.impls.ManagedObjectTypeProcessorImpl;
import com.hyper9.simdk.codegen.impls.PropertyMappingImpl;
import com.hyper9.simdk.codegen.impls.VimServiceTypeProcessorImpl;
import com.hyper9.simdk.codegen.types.DataObjectType;
import com.hyper9.simdk.codegen.types.DerefInfo;
import com.hyper9.simdk.codegen.types.EnumType;
import com.hyper9.simdk.codegen.types.FaultType;
import com.hyper9.simdk.codegen.types.ManagedObjectType;
import com.hyper9.simdk.codegen.types.MetaProperty;
import com.hyper9.simdk.codegen.types.MetaType;
import com.hyper9.simdk.codegen.types.OutType;
import com.hyper9.simdk.codegen.types.PropertyMapping;
import com.hyper9.simdk.codegen.types.TypeProcessor;
import com.hyper9.simdk.codegen.types.VimServiceType;

/**
 * The class that generates the code.
 * 
 * @author akutz
 * 
 */
public final class CodeGenerator
{
    /**
     * A callback for processing VMware data objects.
     */
    private TypeProcessor<DataObjectType> dataObjectProc =
        new DataObjectTypeProcessorImpl();

    /**
     * A callback for processing VMware faults.
     */
    private TypeProcessor<FaultType> faultProc = new FaultTypeProcessorImpl();

    /**
     * A callback for processing the Vim service type.
     */
    private TypeProcessor<VimServiceType> vimServiceProc =
        new VimServiceTypeProcessorImpl();

    /**
     * A callback for processing VMware managed objects.
     */
    private TypeProcessor<ManagedObjectType> managedObjectProc =
        new ManagedObjectTypeProcessorImpl();

    /**
     * A callback for processing VMware enums.
     */
    private TypeProcessor<EnumType> enumProc = new EnumTypeProcessorImpl();

    /**
     * A map of data objects that have had some of their properties
     * dereferenced. The map is indexed by the name of the data object type
     * name.
     */
    private HashMap<String, List<DerefInfo>> derefedDataObjects =
        new HashMap<String, List<DerefInfo>>();

    /**
     * Do not allow this class to be instantiated via a constructor.
     */
    private CodeGenerator()
    {
    }

    /**
     * Creates a new instance of a CodeGenerator.
     * 
     * @return A new CodeGenerator.
     */
    public static CodeGenerator instance()
    {
        return new CodeGenerator();
    }

    /**
     * Do the code generation.
     */
    public void generate() throws Exception
    {
        makeDirectories();

        processHtmlDir();

        // Read in types.
        readInTypes();

        // If there are dereferenced data objects then we need to dereference
        // their types.
        if (Settings.getVSphereSdkDocs() != null)
        {
            dereferenceTypes();
        }

        // Convert VMware package names to SimDK package names.
        convertPackageNames();

        // Create property mappings.
        createPropertyMappings();

        // Write out types.
        writeOutTypes();
    }

    private void makeDirectories() throws Exception
    {
        File rootPkgDir =
            new File(Settings.getGeneratedSources(), Settings
                .getRootPackageNameSlash());
        File daoDir = new File(rootPkgDir, "dao");
        File maoDir = new File(rootPkgDir, "mao");
        File fltDir = new File(rootPkgDir, "faults");
        File enmDir = new File(rootPkgDir, "enums");

        daoDir.mkdirs();
        maoDir.mkdirs();
        fltDir.mkdirs();
        enmDir.mkdirs();
    }

    /**
     * Processes the vSphere4 Reference Guide HTML files.
     * 
     * @throws Exception When an error occurs.
     */
    private void processHtmlDir() throws Exception
    {
        if (!Settings.getVSphereSdkDocs().exists())
        {
            throw new Exception(Settings.getVSphereSdkDocs()
                + " does not exist");
        }

        File doTypes =
            new File(Settings.getVSphereSdkDocs(), "index-do_types.html");

        if (!doTypes.exists())
        {
            throw new Exception(doTypes + " does not exist");
        }

        Pattern pattern =
            Pattern
                .compile("</nobr><nobr><a title=\\\"([^\\\"]*?)\\\" target=\\\"classFrame\\\" href=\\\"([^\\\"]*?)\\\">([^<]*?)</a>");
        Scanner scanner = new Scanner(doTypes);

        while (scanner.hasNextLine())
        {
            if (scanner.findInLine(pattern) == null)
            {
                scanner.nextLine();
                continue;
            }

            MatchResult matchResult = scanner.match();

            String viSdkTypeSimpleName = matchResult.group(1);
            String viSdkTypeFilePath = matchResult.group(2);

            File viSdkTypeFile =
                new File(Settings.getVSphereSdkDocs(), viSdkTypeFilePath);

            if (!viSdkTypeFile.exists())
            {
                throw new Exception(viSdkTypeFile + " does not exist");
            }

            if (derefedDataObjects.containsKey(viSdkTypeSimpleName))
            {
                throw new Exception("processing " + viSdkTypeSimpleName
                    + " more than once");
            }

            processHtmlFile(viSdkTypeSimpleName, viSdkTypeFile);
        }
        scanner.close();
    }

    private void processHtmlFile(String viSdkTypeSimpleName, File viSdkTypeFile)
        throws Exception
    {
        // Read in the entire file.
        Scanner scanner = new Scanner(viSdkTypeFile);
        scanner.useDelimiter("\\Z");
        String contents = scanner.next();
        scanner.close();

        // Create a pattern that finds all properties that point to a managed
        // object reference and are dereferenced by the document.
        String pattStr =
            "<td nowrap=\"1\"><a id=\"([^\"]*?)\" name=\"[^\"]*?\"></a>"
                + "<strong>[^<]*?</strong>"
                + "(?:<span title=\"Need not be set\" class=\"footnote-ref\">\\*</span>)?"
                + "</td><td><a href=\"(?:[^\"]*?)\">(ManagedObjectReference(?:\\[\\])?)"
                + "</a>\\r\\n<br> to a\\r\\n\\s{18}<a href=\"[^\"]*?\">([^<]*?)</a></td>";
        Pattern pattern = Pattern.compile(pattStr);
        Matcher matcher = pattern.matcher(contents);

        // A list of dereferenced information objects.
        List<DerefInfo> derefInfos = new ArrayList<DerefInfo>();

        while (matcher.find())
        {
            MatchResult matchResult = matcher.toMatchResult();

            String propertyName = matchResult.group(1);
            String morefType = matchResult.group(2);
            String dereferencedTypeName = matchResult.group(3);

            DerefInfo derefInfo = new DerefInfoImpl();

            derefInfo.setPropertyName(propertyName);
            derefInfo.setMorefType(morefType);
            derefInfo.setDerefTypeName(dereferencedTypeName);

            derefInfos.add(derefInfo);
        }

        derefedDataObjects.put(viSdkTypeSimpleName, derefInfos);
    }

    private void dereferenceTypes()
    {
        // Add in the internal types.
        addInternalTypesToDereference(derefedDataObjects);

        dereferenceTypes(derefedDataObjects, dataObjectProc
            .getProcessedMetaTypes());
    }

    private void addInternalTypesToDereference(
        HashMap<String, List<DerefInfo>> toDeref)
    {
        addInternalServiceContentToDereference(toDeref);
    }

    private void addInternalServiceContentToDereference(
        HashMap<String, List<DerefInfo>> toDeref)
    {
        List<DerefInfo> derefInfoList = new ArrayList<DerefInfo>();

        DerefInfo di1 = new DerefInfoImpl();
        di1.setPropertyName("agentManager");
        di1.setMorefType("ManagedObjectReference");
        di1.setDerefTypeName("AgentManager");

        DerefInfo di2 = new DerefInfoImpl();
        di2.setPropertyName("diskManager");
        di2.setMorefType("ManagedObjectReference");
        di2.setDerefTypeName("HostDiskManager");

        DerefInfo di3 = new DerefInfoImpl();
        di3.setPropertyName("nfcService");
        di3.setMorefType("ManagedObjectReference");
        di3.setDerefTypeName("NfcService");

        DerefInfo di4 = new DerefInfoImpl();
        di4.setPropertyName("proxyService");
        di4.setMorefType("ManagedObjectReference");
        di4.setDerefTypeName("ProxyService");

        DerefInfo di5 = new DerefInfoImpl();
        di5.setPropertyName("serviceManager");
        di5.setMorefType("ManagedObjectReference");
        di5.setDerefTypeName("ServiceManager");

        DerefInfo di6 = new DerefInfoImpl();
        di6.setPropertyName("serviceDirectory");
        di6.setMorefType("ManagedObjectReference");
        di6.setDerefTypeName("ServiceDirectory");

        DerefInfo di7 = new DerefInfoImpl();
        di7.setPropertyName("resourcePlanningManager");
        di7.setMorefType("ManagedObjectReference");
        di7.setDerefTypeName("ResourcePlanningManager");

        DerefInfo di8 = new DerefInfoImpl();
        di8.setPropertyName("hostDistributedVirtualSwitchManager");
        di8.setMorefType("ManagedObjectReference");
        di8.setDerefTypeName("HostDistributedVirtualSwitchManager");

        DerefInfo di9 = new DerefInfoImpl();
        di9.setPropertyName("tpmManager");
        di9.setMorefType("ManagedObjectReference");
        di9.setDerefTypeName("HostTpmManager");

        DerefInfo di10 = new DerefInfoImpl();
        di10.setPropertyName("llProvisioningManager");
        di10.setMorefType("ManagedObjectReference");
        di10.setDerefTypeName("HostLowLevelProvisioningManager");

        DerefInfo di11 = new DerefInfoImpl();
        di11.setPropertyName("ftManager");
        di11.setMorefType("ManagedObjectReference");
        di11.setDerefTypeName("HostFaultToleranceManager");

        DerefInfo di12 = new DerefInfoImpl();
        di12.setPropertyName("ilManager");
        di12.setMorefType("ManagedObjectReference");
        di12.setDerefTypeName("ImageLibraryManager");

        DerefInfo di13 = new DerefInfoImpl();
        di13.setPropertyName("internalStatsCollector");
        di13.setMorefType("ManagedObjectReference");
        di13.setDerefTypeName("InternalStatsCollector");

        derefInfoList.add(di1);
        derefInfoList.add(di2);
        derefInfoList.add(di3);
        derefInfoList.add(di4);
        derefInfoList.add(di5);
        derefInfoList.add(di6);
        derefInfoList.add(di7);
        derefInfoList.add(di8);
        derefInfoList.add(di9);
        derefInfoList.add(di10);
        derefInfoList.add(di11);
        derefInfoList.add(di12);
        derefInfoList.add(di13);

        toDeref.put("InternalServiceInstanceContent", derefInfoList);
    }

    private <T extends MetaType> void dereferenceTypes(
        HashMap<String, List<DerefInfo>> toDeref,
        HashMap<String, T> metaTypeMap)
    {
        // Iterate over the list of meta types.
        for (String key : metaTypeMap.keySet())
        {
            // Get the current meta type.
            T t = metaTypeMap.get(key);

            // Get the simple type name of the current meta type.
            String typeSimpleName = t.getOutType().getSimpleName();

            // See if the meta type was dereferenced.
            if (!toDeref.containsKey(typeSimpleName))
            {
                continue;
            }

            // Get the list of dereferenced information objects.
            List<DerefInfo> derefInfoList = toDeref.get(typeSimpleName);

            // Iterate over the list of the meta type's properties.
            for (MetaProperty mp : t.getProperties())
            {
                // Iterate over the list of the dereferenced information
                // objects.
                for (DerefInfo di : derefInfoList)
                {
                    // If the name of the dereferenced property does not match
                    // the name of the current property then continue with the
                    // loop.
                    if (!di.getPropertyName().equals(mp.getName()))
                    {
                        continue;
                    }

                    // Assign the name of the dereferenced type to the
                    // property's simple type name.
                    mp.getOutType().setSimpleName(di.getDerefTypeName());
                    break;
                }
            }
        }
    }

    /**
     * Reads in the types from the VMware internal vim classes.
     * 
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unchecked")
    private void readInTypes() throws Exception
    {
        // Process the "com.vmware.vim" package.
        processInternalVim25StubsPackage("com.vmware.vim", getListOfProcs(
            dataObjectProc,
            faultProc,
            vimServiceProc,
            enumProc));

        // Process the "com.vmware.vim.managedobject" package.
        processInternalVim25StubsPackage(
            "com.vmware.vim.managedobject",
            getListOfProcs(managedObjectProc));
    }

    /**
     * Write the types to disk.
     * 
     * @throws Exception When an error occurs.
     */
    private void writeOutTypes() throws Exception
    {
        writeOutTypes(dataObjectProc.getProcessedMetaTypes());
        writeOutTypes(vimServiceProc.getProcessedMetaTypes());
        writeOutTypes(enumProc.getProcessedMetaTypes());
        writeOutTypes(managedObjectProc.getProcessedMetaTypes());
        writeOutTypes(faultProc.getProcessedMetaTypes());
    }

    /**
     * Creates the property mappings.
     * 
     * @throws Exception When an error occurs.
     */
    private void createPropertyMappings() throws Exception
    {
        createPropertyMappings(managedObjectProc.getProcessedMetaTypes());
        createPropertyMappings(dataObjectProc.getProcessedMetaTypes());
        createPropertyMappings(faultProc.getProcessedMetaTypes());
    }

    @SuppressWarnings("unchecked")
    private <T extends MetaType> void createPropertyMappings(
        HashMap<String, T> metaTypeMap) throws Exception
    {
        Iterator iter = metaTypeMap.keySet().iterator();

        while (iter.hasNext())
        {
            T t = metaTypeMap.get(iter.next());

            for (MetaProperty mp : t.getProperties())
            {
                // If this property is already mapped then do not process it
                // again.
                if (mp.getMapping() != null)
                {
                    continue;
                }

                // Get the package the property's return type belongs to.
                String leftSidePkgName = mp.getOutType().getPackageName();

                // If this property is not one of our generated classes then
                // skip it.
                if (!(leftSidePkgName.equals(Settings.getMaoPackageName())
                    || leftSidePkgName.equals(Settings.getDaoPackageName()) || leftSidePkgName
                    .equals(Settings.getFltPackageName())))
                {
                    continue;
                }

                // Get the component type of the property's out type.
                String leftSideComponentType =
                    TypeUtils.getComponentType(mp.getOutType().getSimpleName());

                // Define a variable to hold the MetaType that the right side of
                // the mapping belongs to.
                MetaType rightSideMetaType = null;

                if (mp.getMetaType().getOutType().getSimpleName().equals(
                    "ResourcePool")
                    && mp.getName().equals("owner"))
                {
                    // Do nothing
                }

                else if (mp.getMetaType().getOutType().getSimpleName().equals(
                    "ComputeResource")
                    && mp.getName().equals("resourcePool"))
                {
                    // Do nothing
                }

                // Get the MetaType of the right-side of the property mapping.
                else if (leftSidePkgName.equals(Settings.getMaoPackageName()))
                {
                    rightSideMetaType =
                        this.managedObjectProc.getProcessedMetaTypes().get(
                            leftSideComponentType);
                }
                else if (leftSidePkgName.equals(Settings.getDaoPackageName()))
                {
                    rightSideMetaType =
                        this.dataObjectProc.getProcessedMetaTypes().get(
                            leftSideComponentType);
                }
                else if (leftSidePkgName.equals(Settings.getFltPackageName()))
                {
                    rightSideMetaType =
                        this.faultProc.getProcessedMetaTypes().get(
                            leftSideComponentType);
                }

                // Define a variable to hold the right-side MetaProperty of the
                // mapping.
                MetaProperty rightSideMetaProp = null;

                // Find the right side of this mapping (if one exists).
                if (rightSideMetaType != null)
                {
                    for (MetaProperty rsmp : rightSideMetaType.getProperties())
                    {
                        String rssn = rsmp.getOutType().getSimpleName();
                        String rsct = TypeUtils.getComponentType(rssn);

                        if (rsct.equals(t.getOutType().getSimpleName()))
                        {
                            rightSideMetaProp = rsmp;
                            break;
                        }
                    }
                }

                // Create the left-side mapping.
                PropertyMapping leftSideMapping = new PropertyMappingImpl();
                leftSideMapping.setLeftSide(mp);
                leftSideMapping.setRightSide(rightSideMetaProp);
                mp.setMapping(leftSideMapping);

                // If the right-side MetaProperty is not null then create a
                // mapping for it as well.
                if (rightSideMetaProp != null)
                {
                    PropertyMapping rightSideMapping =
                        new PropertyMappingImpl();
                    rightSideMapping.setLeftSide(mp);
                    rightSideMapping.setRightSide(rightSideMetaProp);
                    rightSideMetaProp.setMapping(rightSideMapping);
                }
            }
        }
    }

    /**
     * Writes the types to disk.
     * 
     * @param <T> The type of the MetaType.
     * @param metaTypeMap A map of meta types indexed by their simple name.
     * @throws Exception When an error occurs.
     */
    private <T extends MetaType> void writeOutTypes(
        HashMap<String, T> metaTypeMap) throws Exception
    {
        for (String key : metaTypeMap.keySet())
        {
            T t = metaTypeMap.get(key);
            t.writeToFile(Settings.getGeneratedSources().getPath());
        }
    }

    /**
     * Converts the package names of OutTypes from their VMware package name to
     * the SimDK package name.
     */
    private void convertPackageNames()
    {
        convertPackageNames(dataObjectProc.getProcessedMetaTypes());
        convertPackageNames(vimServiceProc.getProcessedMetaTypes());
        convertPackageNames(managedObjectProc.getProcessedMetaTypes());
        convertPackageNames(faultProc.getProcessedMetaTypes());
    }

    /**
     * Converts the package names of OutTypes from their VMware package name to
     * the SimDK package name.
     * 
     * @param <T> The type of the MetaType.
     * @param metaTypeMap A map of meta types indexed by their simple name.
     */
    private <T extends MetaType> void convertPackageNames(
        HashMap<String, T> metaTypeMap)
    {
        // Convert the package names in the meta types.
        for (String key : metaTypeMap.keySet())
        {
            T t = metaTypeMap.get(key);

            for (MetaProperty mp : t.getProperties())
            {
                findAndSetPackageName(mp.getOutType());
            }

            for (OutType ot : t.getOutType().getInterfaces())
            {
                findAndSetPackageName(ot);
            }
        }
    }

    /**
     * Finds the name of the package that the out type belongs to and sets the
     * given out type's package name to said value.
     * 
     * @param outType The out type.
     */
    private void findAndSetPackageName(OutType outType)
    {
        if (outType.getPackageName() != null)
        {
            return;
        }

        if (TypeUtils.getComponentType(outType.getSimpleName()).equals(
            "ManagedObjectReference"))
        {
            outType.setPackageName(Settings.getMaoPackageName());
            return;
        }

        if (TypeUtils.getComponentType(outType.getSimpleName()).equals(
            "DynamicProperty"))
        {
            outType.setPackageName(Settings.getDaoPackageName());
            return;
        }

        String simpleName = outType.getSimpleName();
        simpleName = TypeUtils.getComponentType(outType.getSimpleName());

        if (dataObjectProc.getProcessedMetaTypes().containsKey(simpleName))
        {
            outType.setPackageName(dataObjectProc.getProcessedMetaTypes().get(
                simpleName).getOutType().getPackageName());
        }

        else if (managedObjectProc.getProcessedMetaTypes().containsKey(
            simpleName))
        {
            outType.setPackageName(managedObjectProc
                .getProcessedMetaTypes()
                .get(simpleName)
                .getOutType()
                .getPackageName());
        }

        else if (faultProc.getProcessedMetaTypes().containsKey(simpleName))
        {
            outType.setPackageName(faultProc.getProcessedMetaTypes().get(
                simpleName).getOutType().getPackageName());
        }

        else if (enumProc.getProcessedMetaTypes().containsKey(simpleName))
        {
            outType.setPackageName(enumProc.getProcessedMetaTypes().get(
                simpleName).getOutType().getPackageName());
        }
    }

    /**
     * Gets a list of processors given those in the varargs array.
     * 
     * @param procs An array of processors to be returned as a list.
     * @return A list of processors given those in the varargs array.
     */
    private List<TypeProcessor<? extends MetaType>> getListOfProcs(
        TypeProcessor<? extends MetaType>... procs)
    {
        List<TypeProcessor<? extends MetaType>> list =
            new ArrayList<TypeProcessor<? extends MetaType>>();

        for (TypeProcessor<? extends MetaType> p : procs)
        {
            list.add(p);
        }

        return list;
    }

    /**
     * Enumerates the resources in a given package.
     * 
     * @param dotSeparatedPackageName A fully-qualified Java package path in
     *        dot-notation.
     * @param processors A list of processors to execute on each resource.
     * @throws Exception When an error occurs.
     */
    private void processInternalVim25StubsPackage(
        String dotSeparatedPackageName,
        List<TypeProcessor<? extends MetaType>> processors) throws Exception
    {
        if (processors == null || processors.size() == 0)
        {
            throw new Exception("processors cannot be null or an empty list");
        }

        // Gets the current class loader.
        ClassLoader currentClassLoader =
            Thread.currentThread().getContextClassLoader();

        // Get the slash-separated package name from the dot-separated version.
        String slashSeparatedPackageName =
            dotSeparatedPackageName.replace('.', '/');

        // Get the directory that the package maps to.
        File packageDir =
            new File(Settings.getVSphereStubs(), slashSeparatedPackageName);

        // Get all of the source files in the package.
        String[] sourceFileNames = packageDir.list();

        // Enumerate the source files.
        for (String sourceFileName : sourceFileNames)
        {
            File sourceFile = new File(packageDir, sourceFileName);

            // Ignore non-files.
            if (!sourceFile.isFile())
            {
                continue;
            }

            // Get the class name.
            String className = FilenameUtils.getBaseName(sourceFileName);

            // Get the package and class name.
            String packageAndClassName =
                dotSeparatedPackageName + "." + className;

            // Load the class.
            Class<?> clazz =
                Class.forName(packageAndClassName, false, currentClassLoader);

            // Push the class through the processors to determine if it should
            // be processed by any of them and then doing the processing if
            // necessary.
            for (TypeProcessor<? extends MetaType> proc : processors)
            {
                if (proc.shouldProcess(clazz, sourceFile.getPath()))
                {
                    try
                    {
                        proc.processClass(clazz, sourceFile.getPath());
                        break;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(System.err);
                    }
                }
            }
        }
    }
}
