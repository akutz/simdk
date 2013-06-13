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

package com.hyper9.simdk.stubs.mao;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.xml.bind.annotation.XmlTransient;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import com.hyper9.simdk.stubs.JAXBAnnotated;

/**
 * A managed object reference.
 * 
 * @author akutz
 * 
 */
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@XmlType(name = "ManagedObjectReference", propOrder =
{
    "type", "serverGuid"
})
@JAXBAnnotated
public class ManagedObjectReference implements Serializable
{
    /**
     * The generated serial version UID.
     */
    private static final long serialVersionUID = -6772746181862487603L;

    private String type;
    private String serverGuid;

    private String jpaId;

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

    @Column(name = "C544ab888c6671e1dbaf94a55ec8ba7c8")
    @XmlAttribute(required = false)
    public String getServerGuid()
    {
        return this.serverGuid;
    }

    @Column(name = "C82d8ddd97d43e8cef8b7206f9cf1d100")
    @XmlAttribute(required = true)
    public String getType()
    {
        return this.type;
    }

    @XmlValue
    @Transient
    public String get_Value()
    {
        return getJpaId();
    }

    public void setServerGuid(String toSet)
    {
        this.serverGuid = toSet;
    }

    public void setType(String toSet)
    {
        this.type = toSet;
    }

    public void set_Value(String toSet)
    {
        setJpaId(toSet);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this.getClass() != obj.getClass())
        {
            return false;
        }

        try
        {
            BeanInfo info = Introspector.getBeanInfo(this.getClass());
            PropertyDescriptor[] pdArr = info.getPropertyDescriptors();

            for (PropertyDescriptor pd : pdArr)
            {
                Object pdv1 = pd.getReadMethod().invoke(this, new Object[0]);
                Object pdv2 = pd.getReadMethod().invoke(obj, new Object[0]);

                if (pdv1 == null && pdv2 == null)
                {
                    continue;
                }

                if ((pdv1 == null && pdv2 != null)
                    || (pdv1 != null && pdv2 == null))
                {
                    return false;
                }

                if (!pdv1.equals(pdv2))
                {
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private String sessionKey;

    @XmlTransient
    @Column(name = "Cae1300ef9c06f981a88944ae6969d51c")
    public String getSessionKey()
    {
        return this.sessionKey;
    }

    public void setSessionKey(String toSet)
    {
        this.sessionKey = toSet;
    }
}
