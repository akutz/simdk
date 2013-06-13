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
 * An interface for building the out type.
 * 
 * @author akutz
 * 
 */
public interface OutType
{
    /**
     * Gets the out type's fully package-qualified name.
     * 
     * @return The out type's fully package-qualified name.
     */
    String getName();

    /**
     * Gets the out type's simple name.
     * 
     * @return The out type's simple name.
     */
    String getSimpleName();

    /**
     * Sets the out type's simple name.
     * 
     * @param toSet The out type's simple name.
     */
    void setSimpleName(String toSet);

    /**
     * Gets a value indicating whether or not the out type is abstract.
     * 
     * @return A value indicating whether or not the out type is abstract.
     */
    boolean getIsAbstract();

    /**
     * Sets a value indicating whether or not the out type is abstract.
     * 
     * @param toSet A value indicating whether or not the out type is abstract.
     */
    void setIsAbstract(boolean toSet);

    /**
     * Gets the interfaces to be implemented by the out type.
     * 
     * @return The interfaces to be implemented by the out type.
     */
    List<OutType> getInterfaces();

    /**
     * Gets the out type's superclass.
     * 
     * @return The out type's superclass.
     */
    OutType getSuperclass();

    /**
     * Sets the out type's superclass.
     * 
     * @param toSet The out type's superclass.
     */
    void setSuperclass(OutType toSet);

    /**
     * Gets this out type's package.
     * 
     * @return This out type's package.
     */
    String getPackageName();

    /**
     * Sets this out type's package.
     * 
     * @param toSet This out type's package.
     */
    void setPackageName(String toSet);
}
