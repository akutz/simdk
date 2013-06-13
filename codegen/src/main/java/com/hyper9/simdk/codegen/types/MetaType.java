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

package com.hyper9.simdk.codegen.types;

import java.io.Writer;
import java.util.List;

/**
 * A type that helps in the conversion of an internal VMware type to a SimDK
 * type.
 * 
 * @author akutz
 * 
 */
public interface MetaType
{
    /**
     * Gets the In type.
     * 
     * @return The In type.
     */
    Class<?> getInType();

    /**
     * Gets the path to the source file of the In type.
     * 
     * @return The path to the source file of the In type.
     */
    String getInTypeSourceFilePath();

    /**
     * Gets the Out type.
     * 
     * @return The Out type.
     */
    OutType getOutType();

    /**
     * Gets a string that represents the compilation type (class, interface,
     * enum) of this meta type.
     * 
     * @return A string that represents the compilation type (class, interface,
     *         enum) of this meta type.
     */
    String getCompilationType();

    /**
     * Get this meta type's properties.
     * 
     * @return This meta type's properties.
     */
    List<MetaProperty> getProperties();

    /**
     * Get this meta type's methods.
     * 
     * @return This meta type's methods.
     */
    List<MetaMethod> getMethods();

    /**
     * Gets a list of the types to import for the out type.
     * 
     * @return A list of the types to import for the out type.
     */
    List<String> getOutImports();

    /**
     * Writes the meta type to a file.
     * 
     * @param path The path of the root directory to write the file.
     * @throws Exception When an error occurs.
     */
    void writeToFile(String path) throws Exception;

    /**
     * This method is called by the writeToFile method after the beginning of
     * the compilation type is written and before the end of the compilation
     * type is written.
     * 
     * @param out The writer.
     */
    void writeCompilationTypeContent(Writer out) throws Exception;

    /**
     * Builds the type to be written.
     */
    void build() throws Exception;

    /**
     * Gets a list of this out type's annotations.
     * 
     * @return
     */
    List<String> getAnnotations();

    /**
     * Gets a value indicating whether or not this class is JAXB-annotated.
     * 
     * @return A value indicating whether or not this class is JAXB-annotated.
     */
    boolean isJAXBAnnotated();
}
