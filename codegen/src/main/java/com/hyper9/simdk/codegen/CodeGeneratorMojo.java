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

/**
 * A MOJO for generating code for the SimDK stubs module.
 * 
 * @author akutz
 * 
 * @goal codegen
 * @phase generate-sources
 * @description A MOJO for generating code for the SimDK stubs module.
 * @requiresDependencyResolution
 */
public class CodeGeneratorMojo extends AbstractCodeGenMojo
{
    /**
     * The name of the author to output in generated files.
     * 
     * @parameter
     * @required
     */
    String author;

    /**
     * The path to the internal vSphere stubs.
     * 
     * @parameter
     * @required
     */
    File vSphereStubs;

    /**
     * The path to the vSphere SDK HTML documents.
     * 
     * @parameter
     * @required
     */
    File vSphereSdkDocs;

    /**
     * The table type.
     * 
     * @parameter default-value="JOINED"
     */
    String tableType;

    /**
     * The target namespace.
     * 
     * @parameter
     * @required
     */
    String targetNamespace;

    @Override
    public void doExecute() throws Throwable
    {
        // Initialize the Settings helper class.
        Settings.initialize(this);

        // Do the code generation.
        CodeGenerator.instance().generate();
    }

    @Override
    boolean shouldExecute()
    {
        return !super.generatedSources.exists()
            && !super.generatedResources.exists();
    }
}
