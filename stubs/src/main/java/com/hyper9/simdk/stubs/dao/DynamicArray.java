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

package com.hyper9.simdk.stubs.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import com.hyper9.simdk.stubs.JAXBAnnotated;
import com.hyper9.simdk.stubs.SerializableObjectWrapper;

/**
 * A DynamicArray.
 * 
 * @author akutz
 * 
 */
@XmlType(name = "DynamicArray", propOrder =
{
    "dynamicType", "val"
})
@Entity
@JAXBAnnotated
public class DynamicArray implements Serializable
{
    /**
     * The generated serial version UID.
     */
    private static final long serialVersionUID = 2532488938000052120L;
    
    private String dynamicType;

    @Column(name = "C1416306fc870890348739cf8659b9f4b")
    public String getDynamicType()
    {
        return this.dynamicType;
    }

    public void setDynamicType(String toSet)
    {
        this.dynamicType = toSet;
    }

    private SerializableObjectWrapper valWrapper;
    private List<Object> val;

    public void setVal(List<Object> toSet)
    {
        this.val = toSet;
        this.valWrapper = SerializableObjectWrapper.wrap(toSet);
    }

    @SuppressWarnings("unchecked")
    @Transient
    public List<Object> getVal()
    {
        if (this.val != null)
        {
            return this.val;
        }
        else if (this.valWrapper == null)
        {
            return null;
        }
        else
        {
            this.val = (List<Object>) this.valWrapper.unwrap();
            return this.val;
        }
    }

    public void setValWrapper(SerializableObjectWrapper toSet)
    {
        this.valWrapper = toSet;
    }

    @XmlTransient
    @Column(name = "C26da5d2660fcb20d9018ac4d7fd80129")
    public SerializableObjectWrapper getValWrapper()
    {
        if (this.valWrapper == null)
        {
            return null;
        }
        else
        {
            return this.valWrapper;
        }
    }

    private String jpaId = java.util.UUID.randomUUID().toString();

    @Id
    @Column(name = "jpaId")
    @XmlTransient
    public String getJpaId()
    {
        return this.jpaId;
    }

    public void setJpaId(String toSet)
    {
        this.jpaId = toSet;
    }
}
