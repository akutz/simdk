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
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hyper9.simdk.codegen.Constants;
import com.hyper9.simdk.codegen.types.EnumType;

/**
 * A type processor for Enum types.
 * 
 * @author akutz
 * 
 */
public class EnumTypeProcessorImpl extends TypeProcessorImpl<EnumType>
{
    private final static String ENUM_VAL_PATT =
        "^.*public static final %1$s ([^\\s]*?) = new %1$s\\(\\\"([^\\\"]*)\\\"\\)\\;.*$";

    @Override
    public void processClass(Class<?> clazz, String sourceFilePath)
        throws Exception
    {
        EnumType meta = new EnumTypeImpl(clazz, sourceFilePath);

        // Create the pattern
        String enumValPattStr =
            String.format(ENUM_VAL_PATT, clazz.getSimpleName());
        Pattern enumValPatt = Pattern.compile(enumValPattStr);

        // Open the VimBindingStub source file for reading.
        FileReader fin = new FileReader(sourceFilePath);
        BufferedReader in = new BufferedReader(fin);

        // Read the VimBindingStub source file line-by-line, looking for the
        // methods.
        String line = in.readLine();
        while (line != null)
        {
            Matcher m = enumValPatt.matcher(line);

            if (!m.matches())
            {
                line = in.readLine();
                continue;
            }

            meta.getElements().add(m.group(2));

            line = in.readLine();
        }

        in.close();

        getProcessedMetaTypes().put(meta.getOutType().getSimpleName(), meta);
    }

    @Override
    public boolean shouldProcess(Class<?> clazz, String sourceFilePath)
        throws Exception
    {
        if (clazz.isAssignableFrom(Constants.DYNAMIC_DATA_CLASS)
            || clazz == Constants.VIM_BINDING_STUB_CLASS
            && clazz.getSimpleName().equals("DynamicTypeMapping"))
        {
            return false;
        }

        // Create the pattern
        String enumValPattStr =
            String.format(ENUM_VAL_PATT, clazz.getSimpleName());
        Pattern enumValPatt = Pattern.compile(enumValPattStr);

        // Open the VimBindingStub source file for reading.
        File f = new File(sourceFilePath);

        if (!f.exists())
        {
            return false;
        }

        FileReader fin = new FileReader(sourceFilePath);
        BufferedReader in = new BufferedReader(fin);

        boolean shouldProcess = false;

        // Read the VimBindingStub source file line-by-line, looking for the
        // methods.
        String line = in.readLine();
        while (line != null)
        {
            Matcher m = enumValPatt.matcher(line);

            if (!m.matches())
            {
                line = in.readLine();
                continue;
            }

            shouldProcess = true;
            break;
        }

        in.close();

        return shouldProcess;
    }
}
