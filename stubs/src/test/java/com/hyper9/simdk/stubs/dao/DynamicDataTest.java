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

package com.hyper9.simdk.stubs.dao;

import junit.framework.Assert;
import org.junit.Test;

public class DynamicDataTest
{
    @Test
    public void testEquals1() throws Exception
    {
        AboutInfo ai1 = new AboutInfo();
        ai1.setApiType("1.0");
        
        AboutInfo ai2 = new AboutInfo();
        ai2.setApiType("1.0");
        
        Assert.assertTrue(ai1.equals(ai2));
        
        ai2.setApiType("2.0");
        
        Assert.assertTrue(!ai1.equals(ai2));
        
        ServiceContent sc1 = new ServiceContent();
        sc1.setAbout(ai1);
        
        ServiceContent sc2 = new ServiceContent();
        sc2.setAbout(ai2);
        
        Assert.assertTrue(!sc1.equals(sc2));
        
        ai1.setApiType("2.0");
        
        Assert.assertTrue(sc1.equals(sc2));
    }
}
