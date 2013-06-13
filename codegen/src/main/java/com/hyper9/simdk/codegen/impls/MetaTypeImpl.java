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

package com.hyper9.simdk.codegen.impls;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlType;
import com.hyper9.simdk.codegen.Settings;
import com.hyper9.simdk.codegen.TypeUtils;
import com.hyper9.simdk.codegen.types.MetaMethod;
import com.hyper9.simdk.codegen.types.MetaProperty;
import com.hyper9.simdk.codegen.types.MetaType;
import com.hyper9.simdk.codegen.types.OutType;

public abstract class MetaTypeImpl implements MetaType
{
    /**
     * The comment for a compilation type. This text is not preceded with a
     * newline character but is succeeded by one.
     */
    private static final String COMPILATION_TYPE_COMMENT =
        "/**\n *\n * @author " + Settings.getAuthor() + "\n *\n */\n";

    private Class<?> inType;
    private String inTypeSourceFilePath;
    private List<MetaProperty> properties = new ArrayList<MetaProperty>();
    private OutType outType = new OutTypeImpl();
    private List<MetaMethod> methods = new ArrayList<MetaMethod>();
    private List<String> outImports = new ArrayList<String>();
    private List<String> annotations = new ArrayList<String>();

    /**
     * Creates a new instance of the MetaTypeImpl class.
     * 
     * @param inType The VMware type the MetaType is based on.
     * @param inTypeSourceFilePath The full path to the In type's source file.
     */
    protected MetaTypeImpl(Class<?> inType, String inTypeSourceFilePath)
    {
        this.inType = inType;
        this.inTypeSourceFilePath = inTypeSourceFilePath;
        this.outType.setSimpleName(inType.getSimpleName());
    }

    @Override
    public Class<?> getInType()
    {
        return this.inType;
    }

    @Override
    public String getInTypeSourceFilePath()
    {
        return this.inTypeSourceFilePath;
    }

    @Override
    public void build() throws Exception
    {
        for (MetaProperty mp : this.properties)
        {
            addOutImportType(mp.getOutType());
        }

        for (OutType ot : this.outType.getInterfaces())
        {
            addOutImportType(ot);
        }

        if (isJAXBAnnotated())
        {
            addOutImportType(String.format(
                "%s.%s",
                Settings.getRootPackageName(),
                "JAXBAnnotated"));
            getAnnotations().add("@JAXBAnnotated");
        }
    }

    @Override
    public boolean isJAXBAnnotated()
    {
        return true;
    }

    protected void addXmlTypeName()
    {
        addOutImportType(XmlType.class.getName());
        getAnnotations().add(
            "@XmlType(name=\"" + getOutType().getSimpleName() + "\")");
    }

    protected void addOutImportType(String typeName)
    {
        typeName = TypeUtils.getComponentType(typeName);

        if (!this.outImports.contains(typeName))
        {
            this.outImports.add(typeName);
        }
    }

    /**
     * Adds the out type to the import list if necessary.
     * 
     * @param outType The out type.
     */
    private void addOutImportType(OutType outType) throws Exception
    {
        String morefPatt =
            "(?:ManagedObjectReference(?:\\[\\])?)|(?:(?:List\\<)?ManagedObjectReference(?:\\>)?)";
        String dynPropPatt =
            "(?:DynamicProperty(?:\\[\\])?)|(?:(?:List\\<)?DynamicProperty(?:\\>)?)";
        String methodFaultPatt =
            "(?:MethodFault(?:\\[\\])?)|(?:(?:List\\<)?MethodFault(?:\\>)?)";

        // If the out type is a ManagedObjectReference then go ahead and add it
        // to the import list if not already added.
        if (outType.getSimpleName().matches(morefPatt))
        {
            String name = Settings.getMaoPackageName() + ".ManagedObjectReference";
            addOutImportType(name);
            return;
        }

        if (outType.getSimpleName().matches(dynPropPatt))
        {
            String name = Settings.getDaoPackageName() + ".DynamicProperty";
            addOutImportType(name);
            return;
        }

        if (outType.getSimpleName().matches(methodFaultPatt))
        {
            String name = Settings.getFltPackageName() + ".MethodFault";
            addOutImportType(name);
            return;
        }

        // Get the out type's name.
        String outTypeName = outType.getSimpleName();
        outTypeName = TypeUtils.getComponentType(outType.getSimpleName());

        // If the out type's package name is null or the package then out type
        // belongs to is java.lang then do not add the out type to the resource
        // list.
        if (outType.getPackageName() == null
            || outType.getPackageName().startsWith("java.lang"))
        {
            return;
        }

        // If the out type has not been added to the import list and the out
        // type is not a member of this package then add the out type to the
        // import list.
        if (!this.outType.getPackageName().equals(outType.getPackageName()))
        {
            addOutImportType(outType.getPackageName() + "." + outTypeName);
        }
    }

    @Override
    public OutType getOutType()
    {
        return this.outType;
    }

    @Override
    public List<MetaProperty> getProperties()
    {
        return this.properties;
    }

    @Override
    public List<MetaMethod> getMethods()
    {
        return this.methods;
    }

    protected static String upCaseFirstChar(String toUpCase)
    {
        if (toUpCase == null || toUpCase.equals(""))
        {
            return toUpCase;
        }

        String upCased =
            toUpCase.substring(0, 1).toUpperCase() + toUpCase.substring(1);

        return upCased;
    }

    protected String getPathToOutFile(String rootPath)
    {
        String outFilePath =
            rootPath + "/" + getOutType().getPackageName().replace('.', '/')
                + "/" + getOutType().getSimpleName() + ".java";

        return outFilePath;
    }

    protected boolean isTypeObject(OutType outType)
    {
        return outType.getSimpleName().matches("(?:List\\<)?Object(?:\\>)?");
    }

    @Override
    public List<String> getOutImports()
    {
        return this.outImports;
    }

    @Override
    public List<String> getAnnotations()
    {
        return this.annotations;
    }

    @Override
    public void writeToFile(String path) throws Exception
    {
        build();

        String outFilePath = getPathToOutFile(path);

        FileWriter fstream = new FileWriter(outFilePath);
        BufferedWriter out = new BufferedWriter(fstream);

        // Write the package statement.
        out.write("package " + getOutType().getPackageName() + ";\n\n");

        // Write the import statements.
        List<String> imports = getOutImports();
        for (String i : imports)
        {
            out.write("import " + i + ";\n");
        }
        out.write("\n");

        // Write the compilation unit comment.
        out.write(COMPILATION_TYPE_COMMENT);

        // Write the compilation unit annotations.
        for (String a : getAnnotations())
        {
            out.write(a + "\n");
        }

        // Write the opening part of the compilation unit.
        if (getOutType().getIsAbstract())
        {
            out.write("public abstract " + getCompilationType() + " "
                + getOutType().getSimpleName());
        }
        else
        {
            out.write("public " + getCompilationType() + " "
                + getOutType().getSimpleName());
        }

        // Write the extends.
        if (getOutType().getSuperclass() != null)
        {
            out.write(" extends "
                + getOutType().getSuperclass().getSimpleName());
        }

        // Write the implements.
        if (getOutType().getInterfaces().size() > 0)
        {
            out.write(" implements ");

            // Print the remaining interfaces.
            for (OutType inter : getOutType().getInterfaces())
            {
                out.write(inter.getSimpleName());

                // If this is not the last interface in the list then print out
                // a
                // comma.
                if (inter != getOutType().getInterfaces().get(
                    getOutType().getInterfaces().size() - 1))
                {
                    out.write(", ");
                }
            }
        }

        out.write("\n{\n");

        writeCompilationTypeContent(out);

        out.write("}");
        out.close();
    }
}
