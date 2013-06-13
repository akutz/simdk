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
 * A property for a MetaBean.
 * 
 * @author akutz
 * 
 */
public interface MetaProperty extends Comparable<MetaProperty>
{
    /**
     * Get the MetaType that this MetaProperty belongs to.
     */
    MetaType getMetaType();

    /**
     * Gets the name of the meta property.
     * 
     * @return The name of the meta property.
     */
    String getName();

    /**
     * Sets the name of the meta property.
     * 
     * @param toSet The name of the meta property.
     */
    void setName(String toSet);

    /**
     * Gets the meta property's Out type.
     * 
     * @return The meta property's Out type.
     */
    OutType getOutType();

    /**
     * Sets the meta property's Out type.
     * 
     * @param toSet The meta property's Out type.
     */
    void setOutType(OutType toSet);

    /**
     * Gets the meta property's order.
     * 
     * @return The meta property's order.
     */
    int getOrder();

    /**
     * Sets the meta property's order.
     * 
     * @param toSet The meta property's order.
     */
    void setOrder(int toSet);

    /**
     * Gets the name of this property's column.
     * 
     * @return The name of this property's column.
     */
    String getColumnName();

    /**
     * Gets this property's mapping.
     * 
     * @return This property's mapping.
     */
    PropertyMapping getMapping();

    /**
     * Sets this property's mapping.
     * 
     * @param toSet This property's mapping.
     */
    void setMapping(PropertyMapping toSet);

    /**
     * Gets a list of the property's getter annotations.
     * 
     * @return A list of the property's getter annotations.
     */
    List<String> getGetterAnnotations();

    /**
     * Gets a list of the property's field annotations.
     * 
     * @return A list of the property's field annotations.
     */
    List<String> getFieldAnnotations();
}
