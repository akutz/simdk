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

package com.hyper9.simdk.codegen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtils
{
    /**
     * Gets the component type of a List type.
     * 
     * @param typeSimpleName A simple type name that is a List. If a List is not
     *        provided the value of typeSimpleName will be returned as is.
     * @return The component type of a List type.
     */
    public static String getComponentType(String typeSimpleName)
    {
        Pattern componentTypePatt = Pattern.compile("List\\<([^\\>]*?)\\>");

        Matcher componentTypeMatcher =
            componentTypePatt.matcher(typeSimpleName);

        if (componentTypeMatcher.matches())
        {
            return componentTypeMatcher.group(1);
        }
        else
        {
            return typeSimpleName;
        }
    }

    /**
     * Gets a flag indicating whether the given simple type name is a List.
     * 
     * @param outTypeSimpleName A simple type name.
     * @return A flag indicating whether the given simple type name is a List.
     */
    public static boolean isList(String typeSimpleName)
    {
        return typeSimpleName.matches("List\\<[^\\>]*?\\>");
    }

    /**
     * Gets a flag indicating whether or not the given simple type name is one
     * of the Java Framework's simple data types.
     * 
     * @param typeSimpleName A simple type name.
     * @return A flag indicating whether or not the given simple type name is
     *         one of the Java Framework's simple data types.
     */
    public static boolean isSimpleDataType(String typeSimpleName)
    {
        return typeSimpleName
            .matches("String|Byte|Short|Integer|Long|Float|Double|Boolean|Character");
    }

    /**
     * Gets a flag indicating whether or no the given simple type name is one of
     * the Java Framework's primitive types.
     * 
     * @param typeSimpleName A simple type name.
     * @return A flag indicating whether or no the given simple type name is one
     *         of the Java Framework's primitive types.
     */
    public static boolean isPrimtiveDataType(String typeSimpleName)
    {
        return typeSimpleName
            .matches("byte|short|int|long|float|double|boolean|char");
    }
    
    public static boolean isTemporalType(String typeSimpleName)
    {
        return typeSimpleName.matches("Date|Calendar|Timestamp");
    }

    /**
     * Takes a primitive type simple name and returns the simple type name of
     * its simple data type equivalent. If no equivalent can be found then the
     * parameter is returned.
     * 
     * @param primitiveTypeSimpleName A simple type name of a Java primitive.
     * @return The simple type name of the given primitive's simple data type
     *         equivalent.
     */
    public static String toSimpleDataType(String primitiveTypeSimpleName)
    {
        if (primitiveTypeSimpleName.equals("byte"))
        {
            return "Byte";
        }
        else if (primitiveTypeSimpleName.equals("short"))
        {
            return "Short";
        }
        else if (primitiveTypeSimpleName.equals("int"))
        {
            return "Integer";
        }
        else if (primitiveTypeSimpleName.equals("long"))
        {
            return "Long";
        }
        else if (primitiveTypeSimpleName.equals("float"))
        {
            return "Float";
        }
        else if (primitiveTypeSimpleName.equals("double"))
        {
            return "Double";
        }
        else if (primitiveTypeSimpleName.equals("boolean"))
        {
            return "Boolean";
        }
        else if (primitiveTypeSimpleName.equals("char"))
        {
            return "Character";
        }

        return primitiveTypeSimpleName;
    }
}
