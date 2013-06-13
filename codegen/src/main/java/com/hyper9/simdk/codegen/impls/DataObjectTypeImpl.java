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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.UUID;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import com.hyper9.simdk.codegen.Settings;
import com.hyper9.simdk.codegen.Constants;
import com.hyper9.simdk.codegen.types.DataObjectType;
import com.hyper9.simdk.codegen.types.MetaProperty;

/**
 * A meta type for VMware data objects.
 * 
 * @author akutz
 * 
 */
public class DataObjectTypeImpl extends MetaClassTypeImpl implements
    DataObjectType
{
    private static final String SESSION_KEY_PATT =
        "\n    private String sessionKey;\n" + "    \n" + "    @XmlTransient\n"
            + "    @Column(name= \"Cf12c99d141c8b8e1190545eb3862fbf6\")\n"
            + "    public String getSessionKey()\n" + "    {\n"
            + "        return this.sessionKey;\n" + "    }\n" + "    \n"
            + "    public void setSessionKey(String toSet)\n" + "    {\n"
            + "        this.sessionKey = toSet;\n" + "    }\n";

    protected static final String ID_PATT =
        "\n    private String jpaId = java.util.UUID.randomUUID().toString();\n"
            + "    \n" + "    @XmlTransient" + "\n    @Id\n"
            + "    @Column(name = \"jpaId\")\n"
            + "    public String getJpaId()\n" + "    {\n"
            + "        return this.jpaId;\n" + "    }\n" + "    \n"
            + "    public void setJpaId(String toSet)\n" + "    {\n"
            + "        this.jpaId = toSet;\n" + "    }\n";

    private static final String ARRAY_OF_IMPL_PATT =
        "package " + Settings.getDaoPackageName() + ";\n" + "\n" + "import "
            + Settings.getRootPackageName() + ".ArrayOf;\n"
            + "import javax.xml.bind.annotation.XmlElement;\n"
            + "import java.util.List;\n" + "import " + Settings.getRootPackageName()
            + ".JAXBAnnotated;\n"
            + "import javax.xml.bind.annotation.XmlType;\n\n"
            + "@JAXBAnnotated\n" + "@XmlType(name=\"ArrayOf%1$s\")\n"
            + "public class ArrayOf%1$s extends ArrayOf<%1$s>\n" + "{\n"
            + "    private static final long serialVersionUID = %2$sL;\n"
            + "\n" + "    @Override\n"
            + "    @XmlElement(name = \"%1$s\", namespace = \""
            + Settings.getTargetNamespace() + "\")\n"
            + "    public List<%1$s> getData()\n" + "    {\n"
            + "        return super.getData();\n" + "    }\n" + "}\n" + "";

    /**
     * Creates a new instance of the DataObjectTypeImpl class.
     * 
     * @param inType The VMware type the MetaType is based on.
     * @param inTypeSourceFilePath The full path to the In type's source file.
     */
    protected DataObjectTypeImpl(Class<?> inType, String inTypeSourceFilePath)
    {
        super(inType, inTypeSourceFilePath);
        getOutType().setPackageName(Settings.getDaoPackageName());
    }

    @Override
    public void build() throws Exception
    {
        super.build();

        // Set the Inheritance type.
        if (getOutType().getSimpleName().equals("DynamicData"))
        {
            addOutImportType(BeanInfo.class.getName());
            addOutImportType(Introspector.class.getName());
            addOutImportType(PropertyDescriptor.class.getName());

            addOutImportType(Inheritance.class.getName());
            addOutImportType(InheritanceType.class.getName());
            addOutImportType(Id.class.getName());
            addOutImportType(XmlTransient.class.getName());

            getAnnotations().add(
                "@Inheritance(strategy=InheritanceType." + Settings.getTableType()
                    + ")");
        }

        if (getProperties().size() == 0 && !(this instanceof FaultTypeImpl))
        {
            addXmlTypeName();
        }
        if (getProperties().size() > 0)
        {
            // Sort the properties collection.
            Collections.sort(getProperties());

            addOutImportType(XmlType.class.getName());
            String xmlTypeInit =
                this instanceof FaultTypeImpl ? "@XmlType(propOrder={"
                    : "@XmlType(name=\"" + getOutType().getSimpleName()
                        + "\", propOrder={";

            // Create a buffer to hold the property order annotation.
            StringBuilder propOrderBuff = new StringBuilder(xmlTypeInit);

            // Enumerate the properties.
            for (MetaProperty mp : getProperties())
            {
                // Add the property to the property order annotation buffer.
                propOrderBuff.append("\"" + mp.getName() + "\"");
                if (!mp.equals(getProperties().get(getProperties().size() - 1)))
                {
                    propOrderBuff.append(", ");
                }
            }

            // Add the property order annotation.
            propOrderBuff.append("})");
            getAnnotations().add(propOrderBuff.toString());
        }
    }

    @Override
    public void writeToFile(String path) throws Exception
    {
        super.writeToFile(path);

        if (this.getClass() != DataObjectTypeImpl.class)
        {
            return;
        }

        String outFilePath = getPathToOutFile(path);

        String metaTypeSimpleName = getOutType().getSimpleName();
        String oldFileName = metaTypeSimpleName + ".java";
        String newFileName = "ArrayOf" + oldFileName;

        String arrayOfOutFilePath =
            outFilePath.replace(oldFileName, newFileName);

        FileWriter fstream = new FileWriter(arrayOfOutFilePath);
        BufferedWriter out = new BufferedWriter(fstream);

        String arrayOfImpl =
            String.format(ARRAY_OF_IMPL_PATT, metaTypeSimpleName, UUID
                .randomUUID()
                .getLeastSignificantBits());

        out.write(arrayOfImpl);
        out.close();
    }

    @Override
    public void writeCompilationTypeContent(Writer out) throws Exception
    {
        super.writeCompilationTypeContent(out);

        if (getOutType().getSimpleName().equals("DynamicData"))
        {
            out.write(ID_PATT);
            out.write("\n\n" + Constants.EQUALS_METHOD + "\n");
        }

        if (getOutType().getSimpleName().equals("DynamicData"))
        {
            out.write(SESSION_KEY_PATT);
        }
    }
}
