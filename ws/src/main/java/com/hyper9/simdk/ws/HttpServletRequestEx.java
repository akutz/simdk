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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A mutable HttpServletRequest.
 * 
 * @author akutz
 * 
 */
public class HttpServletRequestEx extends HttpServletRequestWrapper
{
    private List<Cookie> cookies;
    private Principal user;
    private List<String> roleNames = new ArrayList<String>();
    private String requestUri;
    private HashMap<String, String> headers = new HashMap<String, String>();

    public HttpServletRequestEx(HttpServletRequest request)
    {
        super(request);

        this.requestUri = request.getRequestURI();

        Enumeration<?> headerNames = request.getHeaderNames();
        if (headerNames != null)
        {
            while (headerNames.hasMoreElements())
            {
                Object hno = headerNames.nextElement();
                String hn = String.valueOf(hno);
                this.headers.put(hn, request.getHeader(hn));
            }
        }

        this.cookies = new ArrayList<Cookie>();
        if (request.getCookies() != null)
        {
            for (Cookie c : request.getCookies())
            {
                this.cookies.add(c);
            }
        }
    }

    public HttpServletRequestEx(ServletRequest request)
    {
        this((HttpServletRequest) request);
    }

    public void setContent(String toSet)
    {
        try
        {
            this.contentData = toSet.getBytes();

            String enc = super.getCharacterEncoding();
            if (enc == null)
            {
                enc = "UTF-8";
            }

            this.reader =
                new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(this.contentData),
                    enc));

            this.inputStream = new ServletInputStreamWrapper(this.contentData);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public String getAuthType()
    {
        return "COOKIE_AUTH";
    }

    @Override
    public Principal getUserPrincipal()
    {
        return this.user;
    }

    public void setUserPrincipal(Principal toSet)
    {
        this.user = toSet;
    }

    public List<String> getRoleNames()
    {
        return this.roleNames;
    }

    public void setRoleNames(List<String> toSet)
    {
        this.roleNames = toSet;
    }

    @Override
    public boolean isUserInRole(String role)
    {
        return this.roleNames.contains(role);
    }

    public HashMap<String, String> getHeaders()
    {
        return this.headers;
    }

    public void setHeaders(HashMap<String, String> toSet)
    {
        this.headers = toSet;
    }

    @Override
    public String getHeader(String name)
    {
        if (!name.equalsIgnoreCase("cookie"))
        {
            return this.headers.get(name);
        }

        StringBuilder sb = new StringBuilder();

        synchronized (this.cookies)
        {
            for (Cookie c : this.cookies)
            {
                sb.append(c.getName());
                sb.append("=");
                sb.append(c.getValue());
                sb.append(";");
            }

            return sb.toString();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public long getDateHeader(String name)
    {
        try
        {
            return Date.parse(this.headers.get(name));
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getHeaderNames()
    {
        Enumeration headerNames = new Enumeration()
        {
            Iterator iter = headers.keySet().iterator();

            @Override
            public boolean hasMoreElements()
            {
                return iter.hasNext();
            }

            @Override
            public Object nextElement()
            {
                return iter.next();
            }
        };

        return headerNames;
    }

    @Override
    public int getIntHeader(String name)
    {
        try
        {
            return Integer.parseInt(this.headers.get(name));
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getHeaders(String name)
    {
        if (!name.equalsIgnoreCase("cookie"))
        {
            return new StringTokenizer(this.headers.get(name));
        }

        String cookieString = getHeader("cookie");
        StringTokenizer cookieTokens = new StringTokenizer(cookieString);
        return cookieTokens;
    }

    @Override
    public Cookie[] getCookies()
    {
        synchronized (this.cookies)
        {
            Cookie[] cookieArray = new Cookie[this.cookies.size()];
            this.cookies.toArray(cookieArray);
            return cookieArray;
        }
    }

    public void addCookie(Cookie cookie)
    {
        synchronized (this.cookies)
        {
            removeAllCookiesUnsync(cookie.getName());
            this.cookies.add(cookie);
        }
    }

    public void removeAllCookies(String name)
    {
        synchronized (this.cookies)
        {
            removeAllCookiesUnsync(name);
        }
    }

    private void removeAllCookiesUnsync(String name)
    {
        List<Cookie> toRemove = new ArrayList<Cookie>();
        for (Cookie c : this.cookies)
        {
            if (c.getName().equals(name))
            {
                toRemove.add(c);
            }
        }
        this.cookies.removeAll(toRemove);
    }

    @Override
    public String getRequestURI()
    {
        return this.requestUri;
    }

    public void setRequestURI(String toSet)
    {
        this.requestUri = toSet;
    }

    private ServletInputStream inputStream;
    private BufferedReader reader;
    private byte[] contentData;

    @Override
    public int getContentLength()
    {
        if (this.contentData == null)
        {
            return super.getContentLength();
        }
        else
        {
            return this.contentData.length;
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if (this.inputStream == null)
        {
            return super.getInputStream();
        }
        else
        {
            return this.inputStream;
        }
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        if (this.reader == null)
        {
            return super.getReader();
        }
        else
        {
            return this.reader;
        }
    }

    public String getContent() throws IOException
    {
        String enc = getCharacterEncoding();
        if (enc == null)
        {
            enc = "UTF-8";
        }

        InputStreamReader in = new InputStreamReader(getInputStream(), enc);
        StringWriter writer = new StringWriter();
        char[] buff = new char[1024];
        int len = 0;

        while ((len = in.read(buff)) != -1)
        {
            writer.write(buff, 0, len);
        }

        String content = writer.toString();
        setContent(content);

        return content;
    }

    private class ServletInputStreamWrapper extends ServletInputStream
    {
        private byte[] data;
        private int idx = 0;

        ServletInputStreamWrapper(byte[] data)
        {
            if (data == null)
            {
                data = new byte[0];
            }

            this.data = data;
        }

        @Override
        public int read() throws IOException
        {
            if (idx == data.length)
            {
                return -1;
            }

            return data[idx++];
        }
    }
}
