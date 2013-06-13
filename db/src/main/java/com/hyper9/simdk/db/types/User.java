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

package com.hyper9.simdk.db.types;

import java.io.Serializable;
import java.util.List;

/**
 * A User.
 * 
 * @author akutz
 * 
 */
public interface User extends Serializable
{
    /**
     * Gets the user name.
     * 
     * @param userName The the user name.
     */
    void setUserName(String toSet);

    /**
     * Gets the the user name.
     * 
     * @return The the user name.
     */
    String getUserName();

    /**
     * Gets the MD5-encoded passphrase hash.
     * 
     * @return The MD5-encoded passphrase hash.
     */
    String getMD5PassphraseHash();

    /**
     * Sets the MD5-encoded passphrase hash.
     * 
     * @param toSet The MD5-encoded passphrase hash.
     */
    void setMD5PassphraseHash(String toSet);

    /**
     * Gets a list of this user's roles.
     * 
     * @return A list of this user's roles.
     */
    List<Role> getRoles();

    /**
     * Sets a list of this user's roles.
     * 
     * @param toSet A list of this user's roles.
     */
    void setRoles(List<Role> toSet);
}
