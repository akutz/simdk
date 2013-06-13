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
import com.hyper9.simdk.codegen.types.MetaMethod;
import com.hyper9.simdk.codegen.types.MetaParam;
import com.hyper9.simdk.codegen.types.OutType;

/**
 * A meta method.
 * 
 * @author akutz
 * 
 */
public class MetaMethodImpl implements MetaMethod
{
    private String name;
    private OutType outReturnType = new OutTypeImpl();
    private List<MetaParam> params = new ArrayList<MetaParam>();
    private List<OutType> exceptions = new ArrayList<OutType>();
    private List<String> annotations = new ArrayList<String>();

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public OutType getOutReturnType()
    {
        return this.outReturnType;
    }

    @Override
    public void setName(String toSet)
    {
        this.name = toSet;
    }

    @Override
    public List<MetaParam> getParameters()
    {
        return this.params;
    }

    @Override
    public List<OutType> getExceptions()
    {
        return this.exceptions;
    }

    @Override
    public List<String> getAnnotations()
    {
        return this.annotations;
    }
}
