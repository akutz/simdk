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

package com.hyper9.simdk.stubs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A serialzable object wrapper for java.lang.Object types.
 * 
 * @author akutz
 * 
 */
@Entity
@JAXBAnnotated
public class SerializableObjectWrapper implements Serializable
{
    /**
     * A generated serial version UID.
     */
    private static final long serialVersionUID = 937317800333833685L;

    private byte[] objectStream;

    @Column(name = "Cc4b65a10d9e4273b69ea75993d2006fd", length = 65535)
    public byte[] getObjectStream()
    {
        return this.objectStream;
    }

    public void setObjectStream(byte[] toSet)
    {
        this.objectStream = toSet;
    }

    public static SerializableObjectWrapper wrap(Object toWrap)
    {
        try
        {
            if (toWrap == null)
            {
                return null;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject((Serializable) toWrap);
            out.close();
            SerializableObjectWrapper sow = new SerializableObjectWrapper();
            sow.setObjectStream(bos.toByteArray());
            return sow;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Object unwrap()
    {
        try
        {
            if (this.objectStream == null)
            {
                return null;
            }

            ByteArrayInputStream bis =
                new ByteArrayInputStream(getObjectStream());
            ObjectInput in = new ObjectInputStream(bis);
            Object o = in.readObject();
            in.close();
            return o;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private String jpaId = java.util.UUID.randomUUID().toString();

    @XmlTransient
    @Id
    @Column(name = "jpaId")
    public String getJpaId()
    {
        return this.jpaId;
    }

    public void setJpaId(String toSet)
    {
        this.jpaId = toSet;
    }
}
