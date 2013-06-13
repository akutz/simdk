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

package com.hyper9.simdk.ws.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.log4j.Logger;
import com.hyper9.simdk.ws.HttpServletRequestEx;
import com.hyper9.simdk.ws.HttpServletResponseEx;

public class RequestAndResponseFilter implements Filter
{
    /**
     * The logger.
     */
    private static Logger logger =
        Logger.getLogger(RequestAndResponseFilter.class);

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
        logRequest((HttpServletRequestEx) request);

        HttpServletResponseEx httpRespEx = new HttpServletResponseEx(response);

        chain.doFilter(request, httpRespEx);

        logResponse(httpRespEx);

        httpRespEx.fillHttpResponse(response);
    }

    private void logRequest(HttpServletRequestEx request) throws IOException
    {
        String content = request.getContent();
        logger.debug("");
        logger.debug("");
        logger.debug("REQUEST");
        logger.debug("-------");
        logger.debug(content);
    }

    private void logResponse(HttpServletResponseEx response) throws IOException
    {
        String content = response.getContent();
        logger.debug("");
        logger.debug("");
        logger.debug("RESPONSE");
        logger.debug("--------");
        logger.debug(content);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Do nothing
    }
}
