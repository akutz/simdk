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
 * A class that contains the CodeGeneration settings.
 * 
 * @author akutz
 * 
 */
public abstract class Settings
{
    private static CodeGeneratorMojo mojo;

    public static void initialize(CodeGeneratorMojo mojo)
    {
        Settings.mojo = mojo;
    }

    /**
     * Gets the output directory for generated sources.
     * 
     * @return The output directory for generated sources.
     */
    public static File getGeneratedSources()
    {
        return mojo.generatedSources;
    }

    /**
     * The name of the author to output in generated files.
     * 
     * @parameter
     */
    public static String getAuthor()
    {
        return mojo.author;
    }

    /**
     * The path to the internal vSphere stubs.
     * 
     * @parameter
     */
    public static File getVSphereStubs()
    {
        return mojo.vSphereStubs;
    }

    /**
     * The path to the vSphere SDK HTML documents.
     * 
     * @parameter
     */
    public static File getVSphereSdkDocs()
    {
        return mojo.vSphereSdkDocs;
    }

    /**
     * The table type.
     * 
     * @parameter default-value="JOINED"
     */
    public static String getTableType()
    {
        return mojo.tableType;
    }

    /**
     * The target namespace.
     * 
     * @parameter
     */
    public static String getTargetNamespace()
    {
        return mojo.targetNamespace;
    }

    /**
     * The name of the root package.
     * 
     * @parameter
     */
    public static String getRootPackageName()
    {
        return mojo.rootPackageName;
    }

    /**
     * Gets the dao package name in dot-separated notation.
     * 
     * @return The dao package name in dot-separated notation.
     */
    public static String getDaoPackageName()
    {
        return String.format("%s.dao", getRootPackageName());
    }

    /**
     * Gets the mao package name in dot-separated notation.
     * 
     * @return The mao package name in dot-separated notation.
     */
    public static String getMaoPackageName()
    {
        return String.format("%s.mao", getRootPackageName());
    }

    /**
     * Gets the enums package name in dot-separated notation.
     * 
     * @return The enums package name in dot-separated notation.
     */
    public static String getEnmPackageName()
    {
        return String.format("%s.enums", getRootPackageName());
    }

    /**
     * Gets the faults package name in dot-separated notation.
     * 
     * @return The enums package name in dot-separated notation.
     */
    public static String getFltPackageName()
    {
        return String.format("%s.faults", getRootPackageName());
    }

    private static String slash(String dotSeparatedPackageName)
    {
        return dotSeparatedPackageName.replace('.', '/');
    }

    /**
     * Gets the name of the root package in slash-separated notation.
     * 
     * @returns The name of the root package in slash-separated notation.
     */
    public static String getRootPackageNameSlash()
    {
        return slash(getRootPackageName());
    }

    /**
     * Gets the dao package name in slash-separated notation.
     * 
     * @return The dao package name in slash-separated notation.
     */
    public static String getDaoPackageNameSlash()
    {
        return slash(getDaoPackageName());
    }

    /**
     * Gets the mao package name in slash-separated notation.
     * 
     * @return The mao package name in slash-separated notation.
     */
    public static String getMaoPackageNameSlash()
    {
        return slash(getMaoPackageName());
    }

    /**
     * Gets the enums package name in slash-separated notation.
     * 
     * @return The enums package name in slash-separated notation.
     */
    public static String getEnmPackageNameSlash()
    {
        return slash(getEnmPackageName());
    }

    /**
     * Gets the faults package name in slash-separated notation.
     * 
     * @return The enums package name in slash-separated notation.
     */
    public static String getFltPackageNameSlash()
    {
        return slash(getFltPackageName());
    }
}
