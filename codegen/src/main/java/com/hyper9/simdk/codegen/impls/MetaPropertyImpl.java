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

package com.hyper9.simdk.codegen.impls;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import com.hyper9.simdk.codegen.types.MetaProperty;
import com.hyper9.simdk.codegen.types.MetaType;
import com.hyper9.simdk.codegen.types.OutType;
import com.hyper9.simdk.codegen.types.PropertyMapping;

public class MetaPropertyImpl implements MetaProperty
{
    private String name;
    private int order;
    private OutType outType = new OutTypeImpl();
    private MetaType metaType;
    private PropertyMapping propMap;
    private List<String> getterAnnotations = new ArrayList<String>();
    private List<String> fieldAnnotations = new ArrayList<String>();

    public MetaPropertyImpl(MetaType metaType)
    {
        this.metaType = metaType;
    }

    @Override
    public MetaType getMetaType()
    {
        return this.metaType;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public int getOrder()
    {
        return this.order;
    }

    @Override
    public OutType getOutType()
    {
        return this.outType;
    }

    @Override
    public void setName(String toSet)
    {
        this.name = toSet;
    }

    @Override
    public void setOrder(int toSet)
    {
        this.order = toSet;
    }

    @Override
    public void setOutType(OutType toSet)
    {
        this.outType = toSet;
    }

    @Override
    public int compareTo(MetaProperty toCompare)
    {
        if (this.order < toCompare.getOrder())
        {
            return -1;
        }
        else if (this.order == toCompare.getOrder())
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    @Override
    public String getColumnName()
    {
        String metaTypeSimpleName = this.metaType.getOutType().getSimpleName();
        String propName = getName();

        String format = "%s_%s";
        String name = String.format(format, metaTypeSimpleName, propName);
        
        name = "C" + DigestUtils.md5Hex(name);

        return name;
    }

    @Override
    public PropertyMapping getMapping()
    {
        return this.propMap;
    }

    @Override
    public void setMapping(PropertyMapping toSet)
    {
        this.propMap = toSet;
    }

    @Override
    public List<String> getGetterAnnotations()
    {
        return this.getterAnnotations;
    }

    @Override
    public List<String> getFieldAnnotations()
    {
        return this.fieldAnnotations;
    }
}
