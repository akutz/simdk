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

package com.hyper9.simdk.db;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The application's main entry point.
 * 
 * @author akutz
 * 
 */
public class App
{
    /**
     * The command-line options.
     */
    private static Options cliOptions = new Options();

    private static String hibernateConfig;

    public static void main(String[] args)
    {
        setupCLIOpts();

        if (!processCLIOpts(args))
        {
            return;
        }

        if (hibernateConfig != null)
        {
            HibernateUtil.setConfigFilePath(hibernateConfig);
        }
        
        HibernateUtil.getSessionFactory();
    }

    @SuppressWarnings("static-access")
    private static void setupCLIOpts()
    {
        Option hibernateConfigOption =
            OptionBuilder
                .withLongOpt("hibernateConfig")
                .withDescription(
                    "the path to a hibernate configuration file used to persist the collected data")
                .isRequired(false)
                .hasArg(true)
                .withArgName("hibernateConfig")
                .create("h");
        cliOptions.addOption(hibernateConfigOption);
    }

    private static boolean processCLIOpts(String[] args)
    {
        try
        {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(cliOptions, args);

            if (line.hasOption("h"))
            {
                hibernateConfig = line.getOptionValue("h");
            }

            return true;
        }
        catch (ParseException exp)
        {
            System.err.println();
            System.err.println(exp.getMessage());
            printUsageAndHelp();
            return false;
        }
    }

    private static void printUsageAndHelp()
    {
        System.out.println();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("db", cliOptions, true);
        System.out.println();
    }
}
