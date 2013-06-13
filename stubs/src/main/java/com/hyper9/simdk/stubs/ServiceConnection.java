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

import java.rmi.RemoteException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.BindingProvider;
import com.hyper9.simdk.stubs.dao.InternalServiceInstanceContent;
import com.hyper9.simdk.stubs.dao.ServiceContent;
import com.hyper9.simdk.stubs.faults.InvalidLocale;
import com.hyper9.simdk.stubs.faults.InvalidLogin;
import com.hyper9.simdk.stubs.faults.RuntimeFault;
import com.hyper9.simdk.stubs.mao.ServiceInstance;
import com.sun.xml.ws.developer.UsesJAXBContextFeature;

/**
 * A VI/vSphere service connection that is able to access all of vSphere's
 * public and hidden/internal methods.
 * 
 * @author akutz
 * 
 */
public class ServiceConnection
{
    /**
     * Do not allow this class to be initialized with a constructor.
     */
    private ServiceConnection()
    {
    }

    /**
     * The VimService port.
     */
    private VimService vimService;

    /**
     * The ServiceInstance.
     */
    private ServiceInstance serviceInstance;

    /**
     * The ServiceContent.
     */
    private ServiceContent serviceContent;

    /**
     * The InternalServiceContent.
     */
    private InternalServiceInstanceContent internalServiceContent;

    /**
     * Gets the VimService instance.
     * 
     * @return The VimService instance.
     */
    public VimService getVimService()
    {
        return this.vimService;
    }

    /**
     * Gets the ServiceInstance.
     * 
     * @return The ServiceInstance.
     */
    public ServiceInstance getServiceInstance()
    {
        return this.serviceInstance;
    }

    /**
     * Gets the ServiceContent.
     * 
     * @return The ServiceContent.
     */
    public ServiceContent getServiceContent()
    {
        return this.serviceContent;
    }

    /**
     * Gets the InternalServiceContent.
     * 
     * @return The InternalServiceContent.
     */
    public InternalServiceInstanceContent getInternalServiceContent()
    {
        return this.internalServiceContent;
    }

    /**
     * Create a new instance of the ServiceConnection class.
     * 
     * @param server The server to connect to.
     * @param port The port to connect to. Specify null to use the default port
     *        for the given protocol (assumed to be HTTP when the useSsl
     *        parameter is not set to true; otherwise HTTPS).
     * @param userName The user name to connect with.
     * @param password The password to connect with.
     * @param locale The locale. Specifying null results in the default locale
     *        of the remote server being used.
     * @param useSsl Whether or not to use SSL.
     * @param ignoreSslWarnings Whether or not to ignore SSL warnings when the
     *        server's certificate is unknown.
     * @return A new VIClient object.
     * @throws RemoteException When a remote exception occurs.
     * @throws RuntimeFault When a runtime fault occurs.
     * @throws InvalidLocale When an invalid locale is specified.
     * @throws InvalidLogin When invalid credentials are specified.
     */
    public static ServiceConnection open(
        String server,
        Integer port,
        String userName,
        String password,
        String locale,
        boolean useSsl,
        boolean ignoreSslWarnings)
        throws InvalidLogin,
        InvalidLocale,
        RuntimeFault,
        RemoteException
    {
        ServiceConnection sc = new ServiceConnection();

        // Create a VimServiceClient for communicating with the server.
        VimServiceClient vsc = new VimServiceClient();

        if (ignoreSslWarnings)
        {
            // Trust all SSL certificates.
            trustAllHttpsCertificates();
            HttpsURLConnection
                .setDefaultHostnameVerifier(new VerifiesAllHosts());
        }

        // Tell the client to use our own custom JAXBContext factory.
        UsesJAXBContextFeature ujcf =
            new UsesJAXBContextFeature(JAXBContextEx.class);

        // Get the port.
        sc.vimService = vsc.getVimPort(ujcf);

        // Set the endpoint address.
        ((BindingProvider) sc.vimService).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            getUrl(server, port, useSsl));

        // Tell the client to maintain state (respect Set-Cookie headers).
        ((BindingProvider) sc.vimService).getRequestContext().put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY,
            true);

        // Spoof the VI Client's SOAPAction header.
        ((BindingProvider) sc.vimService).getRequestContext().put(
            BindingProvider.SOAPACTION_USE_PROPERTY,
            true);
        ((BindingProvider) sc.vimService).getRequestContext().put(
            BindingProvider.SOAPACTION_URI_PROPERTY,
            "urn:internalvim25/4.0");

        sc.serviceInstance = new ServiceInstance();
        sc.serviceInstance.set_Value("ServiceInstance");
        sc.serviceInstance.setType("ServiceInstance");

        // Retrieve the ServiceContent from the VI server.
        sc.serviceContent =
            sc.vimService.retrieveServiceContent(sc.serviceInstance);

        // Retrieve the InternalServiceContent from the VI server.
        sc.internalServiceContent =
            sc.vimService.retrieveInternalContent(sc.serviceInstance);

        // Authenticate to the VI server.
        sc.vimService.login(
            sc.serviceContent.getSessionManager(),
            userName,
            password,
            locale);

        return sc;
    }

    /**
     * Gets the VI SDK URL.
     * 
     * @param server The server to connect to.
     * @param port The port to connect to.
     * @param useSsl Whether or not to use SSL.
     * @return The VI SDK URL.
     */
    private static String getUrl(String server, Integer port, boolean useSsl)
    {
        String url =
            String.format(
                "%s://%s:%s/sdk",
                useSsl ? "https" : "http",
                server,
                port != null ? port : useSsl ? 443 : 80);
        return url;
    }

    /**
     * Invoking this method causes all server certificates to be trusted.
     */
    private static void trustAllHttpsCertificates()
    {
        try
        {
            TrustManager[] trustAllCerts = new TrustManager[1];
            trustAllCerts[0] = new TrustAllManager();
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (Exception e)
        {
            // Swallow the exception
        }
    }

    /**
     * A host name verifier that verifies all host names.
     * 
     * @author akutz
     * 
     */
    private static class VerifiesAllHosts implements HostnameVerifier
    {
        @Override
        public boolean verify(String urlHostName, SSLSession session)
        {
            return true;
        }
    }

    /**
     * A certificate trust manager that trusts all certificates.
     * 
     * @author akutz
     * 
     */
    private static class TrustAllManager implements X509TrustManager
    {
        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType)
            throws CertificateException
        {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType)
            throws CertificateException
        {
        }
    }
}
