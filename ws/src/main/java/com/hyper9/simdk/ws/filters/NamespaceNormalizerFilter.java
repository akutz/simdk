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

package com.hyper9.simdk.ws.filters;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import com.hyper9.simdk.ws.HttpServletRequestEx;
import com.hyper9.simdk.ws.HttpServletResponseEx;

// TODO: Either make this filter more general purpose or create two filters.

public class NamespaceNormalizerFilter implements Filter
{
    @Override
    public void destroy()
    {
        // Do nothing
    }

    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException
    {
        // Cast the HTTP request to our own version.
        HttpServletRequestEx httpReqEx = (HttpServletRequestEx) request;

        // Cast the HTTP response to our own version.
        HttpServletResponseEx httpRespEx = new HttpServletResponseEx(response);

        // Define a variable to store the original VIM namespace.
        String vimNamespace = null;

        try
        {
            // Normalize all incoming VIM namespaces to urn:internalvim25.
            vimNamespace = normalizeVimNamespace(httpReqEx);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        // Continue with the filter chain.
        chain.doFilter(httpReqEx, httpRespEx);

        if (vimNamespace == null)
        {
            return;
        }

        try
        {
            // Change the VIM namespaces in the response back to the original
            // namespace.
            denormalizeVimNamespace(response, httpRespEx, vimNamespace);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    private String normalizeVimNamespace(HttpServletRequestEx request)
        throws Exception
    {
        String content = request.getContent();

        String vimPattStr = "(urn\\:vim2(?:5)?)";
        Pattern vimPatt = Pattern.compile(vimPattStr);
        Matcher vimMatcher = vimPatt.matcher(content);

        String vimNamespace = null;

        if (vimMatcher.find())
        {
            vimNamespace = vimMatcher.group(1);
            content = content.replaceAll(vimPattStr, "urn:internalvim25");
            request.setContent(content);
        }

        return vimNamespace;
    }

    private void denormalizeVimNamespace(
        ServletResponse response,
        HttpServletResponseEx responseEx,
        String vimNamespace) throws Exception
    {
        String content = responseEx.getContent();

        Pattern soapenvPatt =
            Pattern
                .compile("xmlns\\:([^\\=]*?)\\=\\\"http\\://schemas\\.xmlsoap\\.org/soap/envelope/");
        Matcher soapenvMatcher = soapenvPatt.matcher(content);

        // Sanitize the soapenv namespace.
        if (soapenvMatcher.find())
        {
            String soapenvNS = soapenvMatcher.group(1);

            content =
                content.replaceAll(
                    "xmlns\\:" + soapenvNS + "\\=",
                    "xmlns:soapenv=");
            content =
                content.replaceAll("\\<" + soapenvNS + "\\:", "<soapenv:");
            content =
                content.replaceAll("\\</" + soapenvNS + "\\:", "</soapenv:");
        }

        Pattern vimnsPatt =
            Pattern.compile("xmlns\\:([^\\=]*?)\\=\\\"urn\\:internalvim25\\\"");
        Matcher vimnsMatcher = vimnsPatt.matcher(content);

        // Sanitize the ns2 namespace.
        if (vimnsMatcher.find())
        {
            String vimns = vimnsMatcher.group(1);

            content = content.replaceAll("xmlns\\:" + vimns + "\\=", "xmlns=");
            content =
                content.replaceAll(
                    "xsi\\:type=\"" + vimns + "\\:",
                    "xsi:type=\"");
            content = content.replaceAll("\\<" + vimns + "\\:", "<");
            content = content.replaceAll("\\</" + vimns + "\\:", "</");
        }

        // Remove any xsi:type declarations on ManagedObjectReferences. They
        // confuse
        // the Perl SDK since it keys on the ManagedObjectRefernce type
        // attribute.

        // TODO: Make this happen only for Perl

        String objXsiTypePatt =
            "obj xmlns\\:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi\\:type=\\\"[^\\\"]*?\\\" serverGuid=\\\"([^\\\"]*?)\\\" type=\"([^\\\"]*?)\\\"";
        content =
            content.replaceAll(
                objXsiTypePatt,
                "obj serverGuid=\"$1\" type=\"$2\"");

        String valXsiTypePatt =
            "val xmlns\\:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi\\:type=\\\"[^\\\"]*?\\\" serverGuid=\\\"([^\\\"]*?)\\\" type=\"([^\\\"]*?)\\\"";
        content =
            content
                .replaceAll(
                    valXsiTypePatt,
                    "val serverGuid=\"$1\" type=\"$2\" xsi:type=\"ManagedObjectReference\"");

        String morefXsiTypePatt =
            "ManagedObjectReference xsi\\:type=\\\"[^\\\"]*?\\\" serverGuid=\\\"([^\\\"]*?)\\\" type=\"([^\\\"]*?)\\\"";
        content =
            content
                .replaceAll(
                    morefXsiTypePatt,
                    "ManagedObjectReference serverGuid=\"$1\" type=\"$2\" xsi:type=\"ManagedObjectReference\"");

        // Replace the internalvim25 namespace with the one that was sent with
        // the request.
        content = content.replaceAll("urn\\:internalvim25", vimNamespace);

        content =
            content.replaceAll(
                "\\<\\?xml version\\='1\\.0' encoding\\='UTF\\-8'\\?\\>",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        content =
            content
                .replaceAll(
                    "xmlns\\:xsi\\=\\\"http\\://www\\.w3\\.org/2001/XMLSchema\\-instance\\\" ",
                    "");

        content =
            content
                .replaceAll(
                    "\\<soapenv\\:Envelope xmlns\\:soapenv\\=\\\"http\\://schemas\\.xmlsoap\\.org/soap/envelope/\\\"\\>",
                    "<soapenv:Envelope xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

        content = content.replaceAll("xs\\:", "xsd:");

        content =
            content.replaceAll(
                " xmlns\\:xs\\=\\\"http\\://www\\.w3\\.org/2001/XMLSchema\\\"",
                "");

        content =
            content
                .replaceAll(
                    "\\<VirtualDevice xsi\\:type\\=\\\"VirtualMachineVMCIDevice\\\"><key>[^\\<]+?\\</key\\>\\<deviceInfo\\>\\<label\\>[^\\<]+?\\</label\\>\\<summary\\>[^\\<]+?\\</summary\\>\\</deviceInfo\\>\\<controllerKey\\>[^\\<]+?\\</controllerKey\\>\\<unitNumber\\>[^\\<]+?\\</unitNumber\\>\\<allowUnrestrictedCommunication\\>[^\\<]+?</allowUnrestrictedCommunication\\>\\</VirtualDevice\\>",
                    "");

        // Fill in the original HTTP response with the data from the overloaded
        // response.
        responseEx.fillHttpResponse(response, content);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Do nothing
    }
}
