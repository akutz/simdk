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

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * A JAX-WS client for the SimDK/vSphere web service.
 * 
 * @author akutz
 * 
 */
@WebServiceClient(name = "VimService", targetNamespace = "urn:internalvim25")
public class VimServiceClient extends Service
{
    private final static URL WSDL;
    private final static QName VIM_SERVICE;
    private final static QName VIM_PORT;

    private static String getUrl()
    {
        String[] parts =
            VimServiceClient.class.getPackage().getName().split("\\.");
        StringBuilder buff = new StringBuilder();
        buff.append("http://");
        for (int x = parts.length - 1; x > -1; --x)
        {
            buff.append(parts[x]);

            if (x > 0)
            {
                buff.append(".");
            }
        }
        buff.append("/");
        return buff.toString();
    }

    static
    {
        WSDL = VimServiceClient.class.getResource("/VimService.wsdl");
        String url = getUrl();
        VIM_SERVICE = new QName(url, "VimService");
        VIM_PORT = new QName(url, "VimPort");
    }

    public VimServiceClient(URL wsdlLocation, QName serviceName)
    {
        super(wsdlLocation, serviceName);
    }

    public VimServiceClient()
    {
        super(WSDL, VIM_SERVICE);
    }

    /**
     * 
     * @return returns VimService
     */
    @WebEndpoint(name = "VimPort")
    public VimService getVimPort()
    {
        return super.getPort(VIM_PORT, VimService.class);
    }

    /**
     * 
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to
     *        configure on the proxy. Supported features not in the
     *        <code>features</code> parameter will have their default values.
     * @return returns VimService
     */
    @WebEndpoint(name = "VimPort")
    public VimService getVimPort(WebServiceFeature... features)
    {
        return super.getPort(VIM_PORT, VimService.class, features);
    }

}
