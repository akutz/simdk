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

import java.util.List;

/**
 * A meta method.
 * 
 * @author akutz
 * 
 */
public interface MetaMethod
{
    /**
     * Get this method's Out return type.
     * 
     * @return This method's Out return type.
     */
    OutType getOutReturnType();

    /**
     * Get a list of this method's parameters.
     * 
     * @return A list of this method's parameters.
     */
    List<MetaParam> getParameters();

    /**
     * Get this method's name.
     * 
     * @return This method's name.
     */
    String getName();

    /**
     * Set this method's name.
     * 
     * @param toSet This method's name.
     */
    void setName(String toSet);

    /**
     * Gets a list of Exceptions this method can throw.
     * 
     * @return A list of Exceptions this method can throw.
     */
    List<OutType> getExceptions();
    
    /**
     * Gets a list of this out type's annotations.
     * 
     * @return
     */
    List<String> getAnnotations();
}
