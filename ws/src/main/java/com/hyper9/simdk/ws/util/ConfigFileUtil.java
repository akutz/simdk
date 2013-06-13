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

import java.io.File;

/**
 * This class is used to help locate configuration files for this web
 * application.
 * 
 * @author akutz
 * 
 */
public final class ConfigFileUtil
{
    private static File configDir;

    /**
     * <p>
     * Gets a configuration file by looking for the file name in the following
     * places in the following order:
     * </p>
     * <ol>
     * <li>$COM.HYPER9.SIMDK.CONFIGDIRROOT</li>
     * <li>/etc/simdk</li>
     * <li>./conf
     * </ol>
     * 
     * @param fileName The name of the file.
     * @return The file if found; otherwise null.
     */
    public static File getFile(String fileName)
    {
        if (configDir == null)
        {
            configDir = getConfigDir();

            if (configDir == null)
            {
                return null;
            }
        }

        System.out.println("using configuration directory="
            + configDir.getAbsolutePath());

        File f = new File(configDir.getAbsolutePath() + "/" + fileName);

        if (f.exists())
        {
            System.out.println("got file=" + f.getAbsolutePath());
            return f;
        }
        else
        {
            System.err.println("error getting file=" + fileName);
            return null;
        }
    }

    private static File getConfigDir()
    {
        String p = System.getenv("COM.HYPER9.SIMDK.CONFIGDIRROOT");

        if (p != null)
        {
            File f = new File(p);

            if (f.exists())
            {
                return f;
            }
            else
            {
                System.err.println("error getting config dir=" + p);
                return null;
            }
        }

        File f = new File("/etc/simdk");

        if (f.exists())
        {
            return f;
        }

        File cd = new File(".");

        p = cd.getAbsolutePath() + "/conf";
        f = new File(p);

        if (f.exists())
        {
            return f;
        }

        p = cd.getAbsolutePath() + "/src/main/webapp/conf";
        f = new File(p);

        if (f.exists())
        {
            return f;
        }
        else
        {
            System.err.println("error getting config dir=" + p);
            return null;
        }
    }
}
