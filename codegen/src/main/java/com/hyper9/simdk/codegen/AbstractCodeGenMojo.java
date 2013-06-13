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
import java.util.List;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * A base MOJO for other CodeGen MOJOs.
 * 
 * @author akutz
 * 
 */
public abstract class AbstractCodeGenMojo extends AbstractMojo
{
    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    MavenProject mavenProject;

    /**
     * The output directory for generated sources.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources"
     */
    File generatedSources;

    /**
     * The output directory for generated resources.
     * 
     * @parameter default-value="${project.build.directory}/generated-resources"
     */
    File generatedResources;

    /**
     * Whether or not this mojo is enabled.
     * 
     * @parameter default-value="true"
     */
    boolean enabled;

    /**
     * Whether or not to force this mojo to execute even if the execution
     * conditions are not met.
     * 
     * @parameter default-value="false"
     */
    boolean force;

    /**
     * The name of the root package.
     * 
     * @parameter
     * @required
     */
    String rootPackageName;

    /**
     * Performs the mojo's executions.
     * 
     * @throws Exception When an error occurs.
     */
    abstract void doExecute() throws Throwable;

    /**
     * Returns a value indicating whether or not the mojo should execute.
     * 
     * @return A value indicating whether or not the mojo should execute.
     */
    abstract boolean shouldExecute();

    @Override
    public final void execute()
        throws MojoExecutionException,
        MojoFailureException
    {
        addGeneratedSources();
        addGeneratedResources();

        if (!this.enabled)
        {
            return;
        }

        if (!this.force && !this.shouldExecute())
        {
            return;
        }

        this.generatedSources.mkdirs();
        this.generatedResources.mkdirs();

        try
        {
            doExecute();
        }
        catch (Throwable e)
        {
            throw new MojoExecutionException("Error executing mojo", e);
        }
    }

    /**
     * Add the generatedSources directory to the build.
     */
    private void addGeneratedSources()
    {
        this.mavenProject.addCompileSourceRoot(this.generatedSources.getPath());
    }

    /**
     * Add the generatedResources directory to the build.
     */
    private void addGeneratedResources()
    {
        List<String> includes = new ArrayList<String>();
        includes.add("**/*");
        Resource resource = new Resource();
        resource.setDirectory(this.generatedResources.getPath());
        resource.setFiltering(false);
        resource.setIncludes(includes);
        this.mavenProject.addResource(resource);
    }
}
