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

package com.hyper9.simdk.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class HttpServletResponseEx extends HttpServletResponseWrapper
{
    private ServletOutputStreamWrapper outputStream =
        new ServletOutputStreamWrapper();

    public HttpServletResponseEx(HttpServletResponse response)
    {
        super(response);
    }

    public HttpServletResponseEx(ServletResponse response)
    {
        this((HttpServletResponse) response);
    }

    public String getContent() throws IOException
    {
        String enc = getCharacterEncoding();
        if (enc == null)
        {
            enc = "UTF-8";
        }

        String content = new String(this.outputStream.getBuffer(), enc);
        return content;
    }

    @Override
    public void flushBuffer() throws IOException
    {
        this.outputStream.flush();
    }

    @Override
    public int getBufferSize()
    {
        return this.outputStream.getBuffer().length;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return new PrintWriter(this.outputStream);
    }

    @Override
    public void reset()
    {
        this.outputStream.reset();
    }

    @Override
    public void resetBuffer()
    {
        this.outputStream.reset();
    }

    @Override
    public void setBufferSize(int size)
    {
        this.outputStream.setBufferSize(size);
    }

    private class ServletOutputStreamWrapper extends ServletOutputStream
    {
        private ByteArrayOutputStream buff = new ByteArrayOutputStream();

        public byte[] getBuffer()
        {
            return this.buff.toByteArray();
        }

        public void write(int data)
        {
            this.buff.write(data);
        }

        public void reset()
        {
            this.buff.reset();
        }

        public void setBufferSize(int size)
        {
            this.buff = new ByteArrayOutputStream(size);
        }
    }

    public void fillHttpResponse(ServletResponse response) throws IOException
    {
        fillHttpResponse(response, getContent());
    }

    public void fillHttpResponse(ServletResponse response, String content)
        throws IOException
    {
        String enc = getCharacterEncoding();
        if (enc == null)
        {
            enc = "utf-8";
        }

        byte[] buff = content.getBytes(enc);

        response.setContentType(getContentType());
        response.setContentLength(buff.length);
        response.getOutputStream().write(buff);
    }
}
