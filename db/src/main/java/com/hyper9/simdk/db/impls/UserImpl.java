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

package com.hyper9.simdk.db.impls;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import com.hyper9.simdk.db.types.Role;
import com.hyper9.simdk.db.types.User;

@Entity
public class UserImpl implements User
{
    private String userName;
    private String md5passphraseHash;
    private List<Role> roles;

    /**
     * The generated serial version UID.
     */
    private static final long serialVersionUID = 6308095009333441016L;

    @Override
    public String getMD5PassphraseHash()
    {
        return this.md5passphraseHash;
    }

    @Override
    @ManyToMany(targetEntity = RoleImpl.class, mappedBy = "users")
    public List<Role> getRoles()
    {
        return this.roles;
    }

    @Id
    @Override
    public String getUserName()
    {
        return this.userName;
    }

    @Override
    public void setMD5PassphraseHash(String toSet)
    {
        this.md5passphraseHash = toSet;
    }

    @Override
    public void setRoles(List<Role> toSet)
    {
        this.roles = toSet;
    }

    @Override
    public void setUserName(String toSet)
    {
        this.userName = toSet;
    }
}
