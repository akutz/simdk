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

/**
 * A MOJO for invoking wsgen.
 * 
 * @author akutz
 * 
 * @goal wsgen
 * @phase process-classes
 * @description A MOJO for invoking wsgen.
 * @requiresDependencyResolution
 */
public class WsgenMojo extends AbstractCodeGenMojo
{
    @SuppressWarnings("unchecked")
    @Override
    public void doExecute() throws Throwable
    {
        Object[] classpathArray =
            this.mavenProject.getCompileClasspathElements().toArray(
                new Object[0]);

        StringBuilder classpathBuff = new StringBuilder();
        for (Object cp : classpathArray)
        {
            classpathBuff.append(cp);
            classpathBuff.append(":");
        }
        classpathBuff.deleteCharAt(classpathBuff.length() - 1);

        String[] args =
            new String[]
            {
                "-keep", "-wsdl", "-d",
                super.mavenProject.getBuild().getDirectory(), "-s",
                this.generatedSources.getPath(), "-r",
                this.generatedResources.getPath(), "-cp",
                classpathBuff.toString(),
                super.rootPackageName + ".VimServiceImpl"
            };

        com.sun.tools.ws.WsGen.doMain(args);
    }

    @Override
    boolean shouldExecute()
    {
        return !super.generatedSources.exists()
            && !super.generatedResources.exists();
    }
}
