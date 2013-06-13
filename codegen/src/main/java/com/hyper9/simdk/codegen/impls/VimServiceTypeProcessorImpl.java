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

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hyper9.simdk.codegen.Constants;
import com.hyper9.simdk.codegen.types.MetaMethod;
import com.hyper9.simdk.codegen.types.MetaParam;
import com.hyper9.simdk.codegen.types.OutType;
import com.hyper9.simdk.codegen.types.VimServiceType;

public class VimServiceTypeProcessorImpl extends
    TypeProcessorImpl<VimServiceType>
{
    /**
     * The regular expression pattern used to match the web methods in the
     * internal VimBindingStub source file.
     */
    private static final Pattern VIM_BINDING_STUB_METHOD_PATT =
        Pattern
            .compile(".*public ([^\\s]+?) ([^\\(]+?)\\(([^\\)]+?)\\) throws RemoteException.*");

    private static HashMap<String, List<String>> methodExceptions =
        new HashMap<String, List<String>>();

    static
    {
        try
        {
            Class<?> vimPortTypeClass =
                Class.forName("com.vmware.vim.VimPortType");
            Method[] vimPortTypeClassMethods = vimPortTypeClass.getMethods();

            for (Method m : vimPortTypeClassMethods)
            {
                Class<?>[] exArr = m.getExceptionTypes();

                if (exArr == null || exArr.length == 0)
                {
                    continue;
                }

                List<String> exList = new ArrayList<String>();

                for (Class<?> ex : exArr)
                {
                    exList.add(ex.getSimpleName());
                }

                methodExceptions.put(m.getName(), exList);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void processClass(Class<?> clazz, String sourceFilePath)
        throws Exception
    {
        VimServiceType meta = new VimServiceTypeImpl(clazz, sourceFilePath);

        // Open the VimBindingStub source file for reading.
        FileReader fin = new FileReader(sourceFilePath);
        BufferedReader in = new BufferedReader(fin);

        // Read the VimBindingStub source file line-by-line, looking for the
        // methods.
        String line = in.readLine();
        while (line != null)
        {
            Matcher m = VIM_BINDING_STUB_METHOD_PATT.matcher(line);

            // If the line doesn't match then read the next line
            // and continue from the beginning of the loop.
            if (!m.matches())
            {
                line = in.readLine();
                continue;
            }

            // Create a new meta method and set its return type and name.
            MetaMethod mm = new MetaMethodImpl();
            mm.setName(m.group(2));
            mm.getOutReturnType().setSimpleName(m.group(1));
          
            // Parse the method's parameters.
            String[] params = m.group(3).split("\\, ");
            for (String p : params)
            {
                String[] ptypeAndName = p.split(" ");

                MetaParam mp = new MetaParamImpl();
                mp.setName(ptypeAndName[1]);
                mp.getOutType().setSimpleName(ptypeAndName[0]);
             
                mm.getParameters().add(mp);
            }

            // Get the method's exceptions and add them to the meta method.
            List<String> exSimpleNameList = methodExceptions.get(mm.getName());

            for (String s : exSimpleNameList)
            {
                OutType outExType = new OutTypeImpl();
                outExType.setSimpleName(s);
                mm.getExceptions().add(outExType);
            }

            // Add the method to the list.
            meta.getMethods().add(mm);

            line = in.readLine();
        }

        // Close the reader.
        fin.close();

        getProcessedMetaTypes().put(meta.getOutType().getSimpleName(), meta);
    }

    @Override
    public boolean shouldProcess(Class<?> clazz, String sourceFilePath) throws Exception
    {
        return clazz == Constants.VIM_BINDING_STUB_CLASS;
    }
}
