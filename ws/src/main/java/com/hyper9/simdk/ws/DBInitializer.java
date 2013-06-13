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

package com.hyper9.simdk.ws;

import java.io.File;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import com.hyper9.simdk.ws.util.ConfigFileUtil;
import com.hyper9.simdk.db.HibernateUtil;
import com.hyper9.simdk.db.impls.SettingImpl;
import com.hyper9.simdk.db.impls.UserImpl;
import com.hyper9.simdk.db.types.Setting;
import com.hyper9.simdk.db.types.User;

/**
 * This class is responsible for initializing the database.
 * 
 * @author akutz
 * 
 */
public class DBInitializer
{
    /**
     * Initializes the database.
     * 
     * @throws Exception When an error occurs.
     */
    public static void init() throws Exception
    {
        File hibConfFile = ConfigFileUtil.getFile("hibernate.cfg.xml");

        if (hibConfFile != null)
        {
            HibernateUtil.setConfigFilePath(hibConfFile.getAbsolutePath());
        }

        // Get a Hibernate session.
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        // Begin a transaction.
        session.beginTransaction();

        // Check to see if the database has already been initialized.
        if (isDBInitialized(session))
        {
            session.getTransaction().commit();
            System.out.println("database has already been initialized");
            return;
        }

        // Initialize the settings.
        initSettings(session);

        // Initialize the default user.
        initDefaultUser(session);

        // Commit the transaction.
        session.getTransaction().commit();
    }

    private static boolean isDBInitialized(Session session)
    {
        // Check to see if the database has already been initialized.
        Query isDBInitializedQuery =
            session.createQuery("FROM " + SettingImpl.class.getSimpleName()
                + " WHERE skey='" + Constants.SETTING_KEY_PREFIX
                + ".db.initialized' AND sval='true'");

        return isDBInitializedQuery.list().size() == 1;
    }

    private static void initDefaultUser(Session session)
    {
        // Create the default user.
        User user = new UserImpl();
        user.setUserName("root");
        user.setMD5PassphraseHash(DigestUtils.md5Hex("password"));
        session.save(user);
    }

    private static void initSettings(Session session)
    {
        // At this point we know the database has not been initialized,
        // so we need to do so.
        addSetting(session, "db.initialized", "true");
        addSetting(session, "login.expires", "1800");
        addSetting(session, "jaas.loginContext", "vim25");
    }

    private static void addSetting(Session session, String key, String value)
    {
        key = Constants.SETTING_KEY_PREFIX + "." + key;
        Setting sb = new SettingImpl();
        sb.setKey(key);
        sb.setValue(value);
        session.save(sb);
    }
}
