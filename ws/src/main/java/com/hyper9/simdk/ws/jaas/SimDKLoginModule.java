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

package com.hyper9.simdk.ws.jaas;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.db.impls.UserImpl;
import com.hyper9.simdk.db.types.Role;
import com.hyper9.simdk.db.types.User;

public class SimDKLoginModule implements LoginModule
{
    private CallbackHandler callbackHandler;
    private Subject subject;
    @SuppressWarnings(
    {
        "unchecked", "unused"
    })
    private Map sharedState;
    @SuppressWarnings(
    {
        "unchecked", "unused"
    })
    private Map options;
    private User user;
    private boolean verified;
    private boolean commitSucceeded;
    private List<Principal> principals = new ArrayList<Principal>();

    @Override
    public boolean abort() throws LoginException
    {
        if (!this.verified)
        {
            return false;
        }
        else if (this.verified && !this.commitSucceeded)
        {
            this.user = null;
            this.verified = false;
            this.principals.clear();
        }
        else
        {
            logout();
        }

        return true;
    }

    @Override
    public boolean commit() throws LoginException
    {
        if (!this.verified)
        {
            return false;
        }

        Principal up = new SimDKUserPrincipal(this.user.getUserName());
        principals.add(up);
        if (!this.subject.getPrincipals().contains(up))
        {
            this.principals.add(up);

            for (Role r : this.user.getRoles())
            {
                Principal rp = new SimDKRolePrincipal(r.getRoleName());
                this.principals.add(rp);
            }

            this.subject.getPrincipals().addAll(this.principals);
        }

        this.user = null;
        this.commitSucceeded = true;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(
        Subject subject,
        CallbackHandler callbackHandler,
        Map sharedState,
        Map options)
    {
        this.callbackHandler = callbackHandler;
        this.subject = subject;
        this.sharedState = sharedState;
        this.options = options;
    }

    @Override
    public boolean login() throws LoginException
    {
        if (this.callbackHandler == null)
        {
            throw new LoginException("callbackHandler is null");
        }

        Callback[] callbacks = new Callback[2];
        NameCallback ncb = new NameCallback("Username: ");
        PasswordCallback pcb = new PasswordCallback("Passphrase: ", false);
        callbacks[0] = ncb;
        callbacks[1] = pcb;

        try
        {
            callbackHandler.handle(callbacks);
            String userName = ncb.getName();
            String md5paswd = DigestUtils.md5Hex(new String(pcb.getPassword()));

            Session session =
                HibernateUtil.getSessionFactory().getCurrentSession();
            String up = "from %s where userName='%s'";
            String uf =
                String.format(up, UserImpl.class.getSimpleName(), userName);
            Query uq = session.createQuery(uf);

            if (uq.list().size() == 0)
            {
                throw new FailedLoginException("no such user: " + userName);
            }

            user = (User) uq.list().get(0);

            if (!md5paswd.equals(user.getMD5PassphraseHash()))
            {
                throw new FailedLoginException("password does not match");
            }

            this.verified = true;
            return true;
        }
        catch (java.io.IOException ioe)
        {
            throw new LoginException(ioe.toString());
        }
        catch (UnsupportedCallbackException uce)
        {
            throw new LoginException("unsupported callgack exception: "
                + uce.getCallback().toString());
        }
        catch (LoginException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw new LoginException("unhandled exception");
        }
        finally
        {
            pcb.clearPassword();
            pcb = null;
        }
    }

    @Override
    public boolean logout() throws LoginException
    {
        this.subject.getPrincipals().removeAll(this.principals);
        this.principals.clear();
        this.verified = false;
        this.commitSucceeded = false;
        this.user = null;
        return true;
    }
}
