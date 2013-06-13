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

package com.hyper9.simdk.codegen;

import java.util.regex.Pattern;

public abstract class Constants
{
    /**
     * A pattern used to match the setFieldName calls in the source files of
     * classes that inherit from DynamicData or MethodFault.
     */
    public static final Pattern FIELD_NAME_PATT =
        Pattern
            .compile("^.*elemField.setFieldName\\(\\\"([^\\\"]+)\\\"\\)\\;$");

    /**
     * The pattern for printing a serial UID.
     */
    public static final String SERIAL_UID_PATT =
        "    /**\n     * A generated serial version UID.\n     */\n"
            + "    private static final long serialVersionUID = %sL;\n\n";

    /**
     * A reference to the class "com.vmware.vim.DynamicData".
     */
    public static Class<?> DYNAMIC_DATA_CLASS;

    /**
     * A reference to the class "com.vmware.vim.managedobject.ManagedObject".
     */
    public static Class<?> MANAGED_OBJECT_CLASS;

    /**
     * A reference to the class "com.vmware.vim.VimBindingStub".
     */
    public static Class<?> VIM_BINDING_STUB_CLASS;

    /**
     * A reference to the class "com.vmware.vim.MethodFault".
     */
    public static Class<?> METHOD_FAULT_CLASS;

    /**
     * A reference to the class "org.apache.axis.AxisFault".
     */
    public static Class<?> AXIS_FAULT_CLASS;

    public static String EQUALS_METHOD =
        "    @Override\n"
            + "    public boolean equals(Object obj)\n"
            + "    {\n"
            + "        if (this.getClass() != obj.getClass())\n"
            + "        {\n"
            + "            return false;\n"
            + "        }\n"
            + "\n"
            + "        try\n"
            + "        {\n"
            + "            BeanInfo info = Introspector.getBeanInfo(this.getClass());\n"
            + "            PropertyDescriptor[] pdArr = info.getPropertyDescriptors();\n"
            + "\n"
            + "            for (PropertyDescriptor pd : pdArr)\n"
            + "            {\n"
            + "\n"
            + "                if (pd.getName().equals(\"jpaId\")) continue;\n\n"
            + "                Object pdv1 = pd.getReadMethod().invoke(this, new Object[0]);\n"
            + "                Object pdv2 = pd.getReadMethod().invoke(obj, new Object[0]);\n\n"
            + "                if (pdv1 == null && pdv2 == null)\n"
            + "                {\n"
            + "                    continue;\n"
            + "                }\n\n"
            + "                if ((pdv1 == null && pdv2 != null) || (pdv1 != null && pdv2 == null))\n"
            + "                {\n" + "                    return false;\n"
            + "                }\n\n"
            + "                if (!pdv1.equals(pdv2))\n"
            + "                {\n" + "                    return false;\n"
            + "                }\n" + "            }\n" + "        }\n"
            + "        catch (Exception e)\n" + "        {\n"
            + "            return false;\n" + "        }\n" + "\n"
            + "        return true;\n" + "    }";

    static
    {
        try
        {
            DYNAMIC_DATA_CLASS = Class.forName("com.vmware.vim.DynamicData");
            MANAGED_OBJECT_CLASS =
                Class.forName("com.vmware.vim.managedobject.ManagedObject");
            VIM_BINDING_STUB_CLASS =
                Class.forName("com.vmware.vim.VimBindingStub");
            METHOD_FAULT_CLASS = Class.forName("com.vmware.vim.MethodFault");
            AXIS_FAULT_CLASS = Class.forName("org.apache.axis.AxisFault");
        }
        catch (Exception e)
        {
            // Do nothing
        }
    }
}
