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

package com.hyper9.simdk.codegen.types;

import java.util.HashMap;

/**
 * A callback used when enumerating package resources and processing meta types.
 * 
 * @author akutz
 * 
 */
public interface TypeProcessor<T extends MetaType>
{
    /**
     * Gets a flag indicating whether or not processClass should be invoked on
     * this class.
     * 
     * @param clazz The class to test.
     * @param sourceFilePath The class's source file path.
     * @return A flag indicating whether or not processClass should be invoked
     *         on this class.
     * @throws Exception When an error occurs.
     */
    boolean shouldProcess(Class<?> clazz, String sourceFilePath)
        throws Exception;

    /**
     * Processes a class that is being enumerated from a package.
     * 
     * @param clazz The class.
     * @param sourceFilePath The class's source file path.
     * @throws Exception When an error occurs.
     */
    void processClass(Class<?> clazz, String sourceFilePath) throws Exception;

    /**
     * Gets a list of the processed MetaTypes indexed by simple name.
     * 
     * @return A list of the processed MetaTypes indexed by simple name.
     */
    HashMap<String, T> getProcessedMetaTypes();
}
