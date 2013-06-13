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

import java.io.Writer;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebFault;
import com.hyper9.simdk.codegen.Settings;
import com.hyper9.simdk.codegen.types.FaultType;

/**
 * A type for representing Vim faults.
 * 
 * @author akutz
 * 
 */
public class FaultTypeImpl extends DataObjectTypeImpl implements FaultType
{
    /**
     * Creates a new instance of the FaultTypeImpl class.
     * 
     * @param inType The VMware type the MetaType is based on.
     * @param inTypeSourceFilePath The full path to the In type's source file.
     */
    protected FaultTypeImpl(Class<?> inType, String inTypeSourceFilePath)
    {
        super(inType, inTypeSourceFilePath);
        getOutType().setPackageName(Settings.getFltPackageName());
    }

    @Override
    public void build() throws Exception
    {
        super.build();

        addOutImportType(WebFault.class.getName());
        String webFaultPatt =
            "@WebFault(name=\"%1$s\", targetNamespace=\"%2$s\")";
        String webFault =
            String.format(webFaultPatt, upCaseFirstChar(getOutType()
                .getSimpleName()), Settings.getTargetNamespace());
        getAnnotations().add(webFault);

        if (getOutType().getSimpleName().equals("MethodFault"))
        {
            addOutImportType(SOAPException.class.getName());

            addOutImportType(Inheritance.class.getName());
            addOutImportType(InheritanceType.class.getName());
            addOutImportType(Id.class.getName());
            addOutImportType(XmlTransient.class.getName());

            getAnnotations().add(
                "@Inheritance(strategy=InheritanceType." + Settings.getTableType()
                    + ")");
        }
    }

    @Override
    public void writeCompilationTypeContent(Writer out) throws Exception
    {
        super.writeCompilationTypeContent(out);

        if (getOutType().getSimpleName().equals("MethodFault"))
        {
            out.write(ID_PATT);
        }
    }
}
