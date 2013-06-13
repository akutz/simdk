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

package com.hyper9.simdk.ws.util;

import java.rmi.RemoteException;
import java.util.Calendar;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import com.hyper9.simdk.ws.Constants;
import com.hyper9.simdk.ws.jaas.SimDKCallbackHandler;
import com.hyper9.simdk.stubs.dao.UserSession;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.db.impls.SettingImpl;
import com.hyper9.simdk.db.types.Setting;
import com.hyper9.simdk.stubs.faults.InvalidLocale;
import com.hyper9.simdk.stubs.faults.InvalidLogin;
import com.hyper9.simdk.stubs.faults.RuntimeFault;
import com.hyper9.simdk.stubs.mao.ManagedObjectReference;

public class SessionManagerUtil
{
    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(SessionManagerUtil.class);

    public static UserSession login(
        String sessionKey,
        ManagedObjectReference _this,
        String userName,
        String password,
        String locale)
        throws RemoteException,
        InvalidLogin,
        InvalidLocale,
        RuntimeFault
    {
        logger.debug(userName + " generated session key=" + sessionKey);

        // Create a new user session.
        UserSession us = new UserSession();
        us.setKey(sessionKey);
        us.setUserName(userName);
        us.setLocale("en_US");
        us.setMessageLocale("en");
        us.setLoginTime(Calendar.getInstance());
        Calendar oneHourAgo = Calendar.getInstance();
        oneHourAgo.roll(Calendar.HOUR, false);
        us.setLastActiveTime(oneHourAgo);

        // Get the current hibernate session.
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        logger.debug("obtained hibernate session");

        // Get the name of the JAAS login context.
        Setting jaasLoginContextSetting =
            (Setting) session.load(
                SettingImpl.class,
                Constants.SETTING_KEY_PREFIX + ".jaas.loginContext");

        logger.debug("got name of JAAS login context="
            + jaasLoginContextSetting.getValue());

        // Attempt to log in the user.
        try
        {
            LoginContext loginContext =
                new LoginContext(
                    jaasLoginContextSetting.getValue(),
                    new SimDKCallbackHandler(userName, password));

            loginContext.login();

            logger.info(userName + " logged in");
        }
        catch (LoginException e)
        {
            logger.error(e);
            throw new InvalidLogin();
        }

        // Indicate that the current session is this one.
        ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getSessionManager()
            .setCurrentSession(us);

        // Add the new user session to the session list.
        ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getSessionManager()
            .getSessionList()
            .add(us);

        // Update the SessionManager.
        session.update(ServiceInstanceUtil
            .getServiceInstance()
            .getContent()
            .getSessionManager());

        logger.debug("saved UserSession");

        return us;
    }

    public static void logout(String sessionKey)
        throws RemoteException,
        RuntimeFault
    {
        // Get the current hibernate session.
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        String sessionKeyColName =
            TypeUtil.getColumnName(UserSession.class, "sessionKey");
        String queryPatt = "DELETE UserSession WHERE %s='%s'";
        String queryString =
            String.format(queryPatt, sessionKeyColName, sessionKey);
        Query query = session.createQuery(queryString);

        int result = query.executeUpdate();

        if (result != 1)
        {
            logger.error("unable to find session key " + sessionKey
                + " to delete");
        }
    }
}
