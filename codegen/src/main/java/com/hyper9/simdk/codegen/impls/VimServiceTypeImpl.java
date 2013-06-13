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

package com.hyper9.simdk.codegen.impls;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.List;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;
import com.hyper9.simdk.codegen.Settings;
import com.hyper9.simdk.codegen.types.MetaMethod;
import com.hyper9.simdk.codegen.types.MetaParam;
import com.hyper9.simdk.codegen.types.OutType;
import com.hyper9.simdk.codegen.types.VimServiceType;

public class VimServiceTypeImpl extends MetaTypeImpl implements VimServiceType
{
    private final static String VIM_SERVICE_SIMPLE_NAME = "VimService";
    private final static String VIM_PORT_TYPE = "VimPortType";
    private final static String VIM_PORT_NAME = "VimPort";
    private VimServiceImpl vimServiceImpl;

    /**
     * Creates a new instance of the VimServiceTypeImpl class.
     * 
     * @param inType The VMware type the MetaType is based on.
     * @param inTypeSourceFilePath The full path to the In type's source file.
     */
    protected VimServiceTypeImpl(Class<?> inType, String inTypeSourceFilePath)
    {
        super(inType, inTypeSourceFilePath);
        getOutType().setSimpleName(VIM_SERVICE_SIMPLE_NAME);
        getOutType().setPackageName(Settings.getRootPackageName());
        this.vimServiceImpl = new VimServiceImpl(inType, inTypeSourceFilePath);
    }
    
    @Override
    public boolean isJAXBAnnotated()
    {
        return false;
    }

    @Override
    public void writeToFile(String path) throws Exception
    {
        super.writeToFile(path);
        vimServiceImpl.writeToFile(path);
    }

    @Override
    public void writeCompilationTypeContent(Writer out) throws Exception
    {
        for (MetaMethod mm : getMethods())
        {
            // Get a reference to the method's parameters.
            List<MetaParam> mpList = mm.getParameters();

            // Create a StringBuilder in order to build the parameter string.
            StringBuilder mpBuff = new StringBuilder();

            // Enumerate the parameters list and build the parameters string.
            for (int x = 0; x < mpList.size(); ++x)
            {
                MetaParam mp = mpList.get(x);

                String paramPatt =
                    "@WebParam(name=\"%2$s\", targetNamespace=\"%3$s\") %1$s %2$s";
                String paramStr =
                    String.format(
                        paramPatt,
                        mp.getOutType().getSimpleName(),
                        mp.getName(),
                        Settings.getTargetNamespace());

                mpBuff.append(paramStr);

                if (x < mpList.size() - 1)
                {
                    mpBuff.append(", ");
                }
            }

            String mpBuffStr = mpBuff.toString();

            /*
             * @RequestWrapper(localName = "SetLocale", targetNamespace =
             * "urn:internalvim25", className = "internalvim25.SetLocale")
             * 
             * @ResponseWrapper(localName = "SetLocaleResponse", targetNamespace
             * = "urn:internalvim25", className =
             * "internalvim25.SetLocaleResponse")
             */
            // Define the pattern that is the method signature.
            String wmPatt =
                "    @WebMethod(operationName=\"%1$s\")\n"
                    + "    @WebResult(targetNamespace=\""
                    + Settings.getTargetNamespace()
                    + "\", name=\"returnval\")\n"
                    + "    @RequestWrapper(localName = \"%1$s\", targetNamespace = \""
                    + Settings.getTargetNamespace()
                    + "\", className = \""
                    + Settings.getRootPackageName()
                    + ".jaxws.%1$s\")\n"
                    + "    @ResponseWrapper(localName = \"%1$sResponse\", targetNamespace = \""
                    + Settings.getTargetNamespace() + "\", className = \""
                    + Settings.getRootPackageName() + ".jaxws.%1$sResponse\")\n"
                    + "    %2$s %3$s(%4$s)%5$s;\n";

            // Get the method's possible exceptions.
            String exStr;

            if (mm.getExceptions().size() == 0)
            {
                exStr = "";
            }
            else
            {
                StringBuilder exBuff = new StringBuilder();
                exBuff.append(" throws ");

                for (OutType exOutType : mm.getExceptions())
                {
                    exBuff.append(exOutType.getSimpleName());

                    if (exOutType != mm.getExceptions().get(
                        mm.getExceptions().size() - 1))
                    {
                        exBuff.append(", ");
                    }
                }

                exStr = exBuff.toString();
            }

            // Build the method signature.
            String wmSig =
                String.format(wmPatt, upCaseFirstChar(mm.getName()), mm
                    .getOutReturnType()
                    .getSimpleName(), mm.getName(), mpBuffStr, exStr);

            // Write the method signature to the file.
            out.write(wmSig);

            // If this isn't the last method in the list then write two
            // line-breaks to separate this method from the next one.
            if (mm != getMethods().get(getMethods().size() - 1))
            {
                out.write("\n");
            }
        }
    }

    @Override
    public String getCompilationType()
    {
        return "interface";
    }

    @Override
    public void build() throws Exception
    {
        super.build();

        addOutImportType(Settings.getMaoPackageName() + ".*");
        addOutImportType(Settings.getDaoPackageName() + ".*");
        addOutImportType(Settings.getEnmPackageName() + ".*");
        addOutImportType(Settings.getFltPackageName() + ".*");
        addOutImportType(WebService.class.getName());
        addOutImportType(WebResult.class.getName());
        addOutImportType(WebMethod.class.getName());
        addOutImportType(WebParam.class.getName());
        addOutImportType(ResponseWrapper.class.getName());
        addOutImportType(RequestWrapper.class.getName());
        addOutImportType(List.class.getName());
        addOutImportType(RemoteException.class.getName());
        addOutImportType("java.util.Calendar");

        getAnnotations().add(
            "@WebService(name = \"" + VIM_PORT_TYPE + "\", "
                + "targetNamespace = \"" + Settings.getTargetNamespace() + "\")");
    }

    private class VimServiceImpl extends MetaTypeImpl implements VimServiceType
    {
        private String OUT_TYPE_IMPL_SIMPLE_NAME =
            VIM_SERVICE_SIMPLE_NAME + "Impl";

        protected VimServiceImpl(Class<?> inType, String inTypeSourceFilePath)
        {
            super(inType, inTypeSourceFilePath);
            getOutType().setSimpleName(OUT_TYPE_IMPL_SIMPLE_NAME);
            getOutType().setPackageName(Settings.getRootPackageName());

            OutType intrface = new OutTypeImpl();
            intrface.setSimpleName("VimService");
            intrface.setPackageName(Settings.getRootPackageName());
            getOutType().getInterfaces().add(intrface);
        }

        @Override
        public String getCompilationType()
        {
            return "class";
        }

        @Override
        public void writeCompilationTypeContent(Writer out) throws Exception
        {
            getMethods().addAll(VimServiceTypeImpl.this.getMethods());

            out
                .write("private static Class<? extends VimServiceEx> vimServiceExClass;\n"
                    + "\n"
                    + "    public static void setVimServiceExClass(Class<? extends VimServiceEx> toSet)\n"
                    + "    {\n"
                    + "        vimServiceExClass = toSet;\n"
                    + "    }\n"
                    + "\n"
                    + "    private VimServiceEx getVimService()\n"
                    + "    {\n"
                    + "        try\n"
                    + "        {\n"
                    + "            Constructor<? extends VimServiceEx> ctor =\n"
                    + "                vimServiceExClass.getConstructor(new Class<?>[0]);\n"
                    + "            return ctor.newInstance(new Object[0]);\n"
                    + "        }\n"
                    + "        catch (Exception e)\n"
                    + "        {\n"
                    + "            e.printStackTrace(System.err);\n"
                    + "            return null;\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    @Resource\n"
                    + "    private WebServiceContext wsContext;\n\n");

            for (MetaMethod mm : getMethods())
            {
                // Get a reference to the method's parameters.
                List<MetaParam> mpList = mm.getParameters();

                // Create a StringBuilder in order to build the parameter
                // strings.
                StringBuilder paramNameAndValBuff = new StringBuilder();
                StringBuilder paramValBuff = new StringBuilder();

                // Enumerate the parameters list and build the parameters
                // string.
                for (int x = 0; x < mpList.size(); ++x)
                {
                    MetaParam mp = mpList.get(x);

                    String paramPatt = "%1$s %2$s";
                    String paramStr =
                        String.format(paramPatt, mp
                            .getOutType()
                            .getSimpleName(), mp.getName());

                    paramNameAndValBuff.append(paramStr);
                    paramValBuff.append(mp.getName());

                    if (x < mpList.size() - 1)
                    {
                        paramNameAndValBuff.append(", ");
                        paramValBuff.append(", ");
                    }
                }

                String paramNameAndValStr = paramNameAndValBuff.toString();
                String paramValStr = paramValBuff.toString();

                // Define the pattern that is the method signature.
                String wmPatt = "";

                if (mm.getOutReturnType().getSimpleName().equals("void"))
                {
                    wmPatt =
                        "    @Override\n"
                            + "    public %1$s %2$s(%3$s)%5$s\n"
                            + "    {\n"
                            + "        VimServiceEx vimService = getVimService();\n"
                            + "        vimService.setWebServiceContext(wsContext);\n"
                            + "        vimService.preMethod();\n"
                            + "        try {vimService.%2$s(%4$s);}\n"
                            + "        finally {vimService.postMethod();}\n"
                            + "    }\n";
                }
                else
                {
                    wmPatt =
                        "    @Override\n"
                            + "    public %1$s %2$s(%3$s)%5$s\n"
                            + "    {\n"
                            + "        VimServiceEx vimService = getVimService();\n"
                            + "        vimService.setWebServiceContext(wsContext);\n"
                            + "        vimService.preMethod();\n"
                            + "        try {return vimService.%2$s(%4$s);}\n"
                            + "        finally {vimService.postMethod();}\n"
                            + "    }\n";
                }

                // Get the method's possible exceptions.
                String exStr;

                if (mm.getExceptions().size() == 0)
                {
                    exStr = "";
                }
                else
                {
                    StringBuilder exBuff = new StringBuilder();
                    exBuff.append(" throws ");

                    for (OutType exOutType : mm.getExceptions())
                    {
                        exBuff.append(exOutType.getSimpleName());

                        if (exOutType != mm.getExceptions().get(
                            mm.getExceptions().size() - 1))
                        {
                            exBuff.append(", ");
                        }
                    }

                    exStr = exBuff.toString();
                }

                // Build the method signature.
                String wmSig =
                    String.format(
                        wmPatt,
                        mm.getOutReturnType().getSimpleName(),
                        mm.getName(),
                        paramNameAndValStr,
                        paramValStr,
                        exStr);

                // Write the method signature to the file.
                out.write(wmSig);

                // If this isn't the last method in the list then write two
                // line-breaks to separate this method from the next one.
                if (mm != getMethods().get(getMethods().size() - 1))
                {
                    out.write("\n");
                }
            }
        }

        @Override
        public void build() throws Exception
        {
            super.build();

            addOutImportType(Settings.getMaoPackageName() + ".*");
            addOutImportType(Settings.getDaoPackageName() + ".*");
            addOutImportType(Settings.getEnmPackageName() + ".*");
            addOutImportType(Settings.getFltPackageName() + ".*");
            addOutImportType(WebService.class.getName());
            addOutImportType(Resource.class.getName());
            addOutImportType(WebServiceContext.class.getName());
            addOutImportType(List.class.getName());
            addOutImportType(RemoteException.class.getName());
            addOutImportType("java.util.Calendar");
            addOutImportType(Constructor.class.getName());
            addOutImportType("com.sun.xml.ws.developer.UsesJAXBContext");

            getAnnotations().add("@UsesJAXBContext(JAXBContextEx.class)");
            getAnnotations().add(
                "@WebService(endpointInterface = \"" + Settings.getRootPackageName()
                    + "." + VIM_SERVICE_SIMPLE_NAME + "\", " + "portName = \""
                    + VIM_PORT_NAME + "\", " + "serviceName = \""
                    + VIM_SERVICE_SIMPLE_NAME + "\")");
        }
        
        @Override
        public boolean isJAXBAnnotated()
        {
            return false;
        }
    }
}
