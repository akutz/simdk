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

import java.util.List;
import javax.xml.bind.JAXBException;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.developer.JAXBContextFactory;

/**
 * A custom JAXB context.
 * 
 * @author akutz
 * 
 */
public class JAXBContextEx implements JAXBContextFactory
{
    @SuppressWarnings("unchecked")
    @Override
    public JAXBRIContext createJAXBContext(
        SEIModel sei,
        List<Class> classesToBind,
        List<TypeReference> typeReferences) throws JAXBException
    {
        addClasses(classesToBind, AllJAXBClasses.getClasses());

        TransientAnnotationReader reader = new TransientAnnotationReader();

        try
        {
            reader.addTransientField(Throwable.class
                .getDeclaredField("stackTrace"));
            reader.addTransientMethod(Throwable.class
                .getDeclaredMethod("getStackTrace"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        JAXBRIContext context =
            JAXBRIContext.newInstance(
                classesToBind.toArray(new Class[classesToBind.size()]),
                typeReferences,
                null,
                sei.getTargetNamespace(),
                false,
                reader);

        return context;
    }

    @SuppressWarnings("unchecked")
    private void addClasses(List<Class> listToAddTo, Class<?>[] toAdd)
    {
        for (Class<?> c : toAdd)
        {
            if (!listToAddTo.contains(c))
            {
                listToAddTo.add(c);
            }
        }
    }
}
