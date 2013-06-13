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

import static com.hyper9.simdk.codegen.TypeUtils.getComponentType;
import static com.hyper9.simdk.codegen.TypeUtils.isList;
import static com.hyper9.simdk.codegen.TypeUtils.isSimpleDataType;
import static com.hyper9.simdk.codegen.TypeUtils.isTemporalType;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import com.hyper9.simdk.codegen.Settings;
import com.hyper9.simdk.codegen.Constants;
import com.hyper9.simdk.codegen.types.MetaProperty;

public abstract class MetaClassTypeImpl extends MetaTypeImpl
{
    /**
     * The format pattern used to print fields.
     */
    private static final String PROP_FLD_PATT =
        "    private %1$s %2$s%3$s;\n\n";

    /**
     * The format pattern used to print property getters.
     */
    private static final String PROP_GET_PATT =
        "    public %1$s get%2$s()\n    {\n        return this.%3$s;\n    }\n\n";

    /**
     * The format pattern used to print property setters.
     */
    private static final String PROP_SET_PATT =
        "    public void set%2$s(%1$s toSet)\n    {\n        this.%3$s = toSet;\n    }\n";

    private static final String eagerCascade =
        "cascade={CascadeType.PERSIST, CascadeType.REFRESH}";

    private static final String saveUpdateCascade =
        "@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)";

    protected MetaClassTypeImpl(Class<?> inType, String inTypeSourceFilePath)
    {
        super(inType, inTypeSourceFilePath);
    }

    @Override
    public void build() throws Exception
    {
        super.build();

        addOutImportType(Entity.class.getName());
        getAnnotations().add("@Entity");

        // Enumerate the properties.
        for (MetaProperty mp : getProperties())
        {
            // Get the property type's simple name.
            String propTypeSimpleName = mp.getOutType().getSimpleName();

            // Get a value indicating whether the property returns an Object.
            boolean isPropTypeObject = isTypeObject(mp.getOutType());

            // Get a value indicating whether the property returns a List.
            boolean isPropTypeList = isList(propTypeSimpleName);

            // Build the column name annotation since so it is used so often in
            // this scope.
            String columnName = "@Column(name=\"" + mp.getColumnName() + "\")";

            // Annotate the property as temporal if it is.
            if (isTemporalType(propTypeSimpleName))
            {
                if (propTypeSimpleName.matches("Date"))
                {
                    mp.getGetterAnnotations().add(
                        "@Temporal(TemporalType.DATE)");
                }
                else if (propTypeSimpleName.matches("Calendar"))
                {
                    mp.getGetterAnnotations().add(
                        "@Temporal(TemporalType.DATE)");
                }
                if (propTypeSimpleName.matches("Timestamp"))
                {
                    mp.getGetterAnnotations().add(
                        "@Temporal(TemporalType.TIME)");
                }

                addOutImportType(Temporal.class.getName());
                addOutImportType(TemporalType.class.getName());
            }

            // List
            if (isPropTypeList)
            {
                addOutImportType(List.class.getName());

                addOutImportType(Settings.getRootPackageName() + ".GenericType");
                mp.getGetterAnnotations().add(
                    "@GenericType(" + getComponentType(propTypeSimpleName)
                        + ".class)");

                addOutImportType(ArrayList.class.getName());
            }

            // Object
            if (isPropTypeObject)
            {
                addOutImportType(XmlTransient.class.getName());
                addOutImportType(Transient.class.getName());
                addOutImportType(Settings.getRootPackageName()
                    + ".SerializableObjectWrapper");
                addOutImportType(CascadeType.class.getName());
                addOutImportType(ManyToOne.class.getName());
                addOutImportType(JoinColumn.class.getName());
                addOutImportType(FetchType.class.getName());
            }

            // List AND Object
            if (isPropTypeList && isPropTypeObject)
            {
                addOutImportType(SuppressWarnings.class.getName());

                mp.getGetterAnnotations().add(
                    "@SuppressWarnings(\"unchecked\")");
            }

            // List AND NOT Object
            if (isPropTypeList && !isPropTypeObject)
            {
                // Mark this property as a collection of elements.
                if (!isMappedProperty(mp))
                {
                    markAsCollOfElems(mp);
                }
            }

            // NOT List AND NOT Object
            if (!isPropTypeList && !isPropTypeObject)
            {
                if (!isMappedProperty(mp))
                {
                    // Import the Column class and add it to the getter.
                    addOutImportType(Column.class.getName());
                    mp.getGetterAnnotations().add(columnName);
                }
            }

            if (isMappedProperty(mp))
            {
                MetaProperty ls = mp.getMapping().getLeftSide();
                MetaProperty rs = mp.getMapping().getRightSide();

                boolean lsIsList = isList(ls.getOutType().getSimpleName());
                String lsName = ls.getName();

                if (mp == ls)
                {
                    // Unidirectional
                    if (rs == null)
                    {
                        // Many-To-Many Unidirectional
                        if (lsIsList)
                        {
                            ls.getGetterAnnotations().add(saveUpdateCascade);

                            addOutImportType(ManyToMany.class.getName());
                            addOutImportType(JoinTable.class.getName());
                            addOutImportType(JoinColumn.class.getName());
                            addOutImportType(CascadeType.class.getName());
                            addOutImportType(FetchType.class.getName());

                            addOutImportType(GenericGenerator.class.getName());
                            String uuidGenName = UUID.randomUUID().toString();
                            String genericGeneratorFormat =
                                "@GenericGenerator(name=\"G%s\", strategy=\"uuid\")";
                            String genericGenerator =
                                String.format(
                                    genericGeneratorFormat,
                                    uuidGenName);
                            ls.getGetterAnnotations().add(genericGenerator);

                            addOutImportType(CollectionId.class.getName());
                            addOutImportType(Column.class.getName());
                            addOutImportType(Type.class.getName());
                            String collectionIdFormat =
                                "@CollectionId(columns=@Column(name=\"ID%s\"),"
                                    + "type=@Type(type=\"string\"),"
                                    + "generator=\"G%s\")";
                            String collectionId =
                                String.format(collectionIdFormat, ls
                                    .getColumnName(), uuidGenName);
                            ls.getGetterAnnotations().add(collectionId);

                            addOutImportType(LazyCollection.class.getName());
                            addOutImportType(LazyCollectionOption.class
                                .getName());
                            ls.getGetterAnnotations().add(
                                "@LazyCollection(LazyCollectionOption.EXTRA)");

                            String joinTableName =
                                DigestUtils.md5Hex(ls.getColumnName());

                            String joinTableFormat =
                                "@JoinTable(name=\"JT%s\", "
                                    + "joinColumns=@JoinColumn(name=\"J%s\", referencedColumnName=\"jpaId\"), "
                                    + "inverseJoinColumns=@JoinColumn(name=\"IJ%s\", referencedColumnName=\"jpaId\"))";

                            String joinTable =
                                String.format(
                                    joinTableFormat,
                                    joinTableName,
                                    ls.getColumnName(),
                                    ls.getColumnName());

                            ls.getGetterAnnotations().add(
                                "@ManyToMany(fetch=FetchType.LAZY, "
                                    + eagerCascade + ")");
                            ls.getGetterAnnotations().add(joinTable);
                        }

                        // Many-To-One Unidirectional
                        else
                        {
                            ls.getGetterAnnotations().add(saveUpdateCascade);

                            addOutImportType(CascadeType.class.getName());
                            addOutImportType(ManyToOne.class.getName());
                            addOutImportType(JoinColumn.class.getName());
                            addOutImportType(FetchType.class.getName());

                            String joinColumnFormat =
                                "@JoinColumn(name=\"J%s\", referencedColumnName=\"jpaId\")";
                            String joinColumn =
                                String.format(joinColumnFormat, ls
                                    .getColumnName());

                            ls.getGetterAnnotations().add(
                                "@ManyToOne(fetch=FetchType.LAZY, "
                                    + eagerCascade + ")");
                            ls.getGetterAnnotations().add(joinColumn);
                        }
                    }
                    else
                    {
                        boolean rsIsList =
                            isList(rs.getOutType().getSimpleName());
                        String rsName = rs.getName();

                        // Many-To-Many
                        if (lsIsList && rsIsList)
                        {
                            addOutImportType(GenericGenerator.class.getName());
                            String uuidGenName = UUID.randomUUID().toString();
                            String genericGeneratorFormat =
                                "@GenericGenerator(name=\"G%s\", strategy=\"uuid\")";
                            String genericGenerator =
                                String.format(
                                    genericGeneratorFormat,
                                    uuidGenName);
                            ls.getGetterAnnotations().add(genericGenerator);

                            addOutImportType(CollectionId.class.getName());
                            addOutImportType(Column.class.getName());
                            addOutImportType(Type.class.getName());
                            String collectionIdFormat =
                                "@CollectionId(columns=@Column(name=\"ID%s\"),"
                                    + "type=@Type(type=\"string\"),"
                                    + "generator=\"G%s\")";
                            String collectionId =
                                String.format(collectionIdFormat, ls
                                    .getColumnName(), uuidGenName);
                            ls.getGetterAnnotations().add(collectionId);

                            addOutImportType(LazyCollection.class.getName());
                            addOutImportType(LazyCollectionOption.class
                                .getName());
                            ls.getGetterAnnotations().add(
                                "@LazyCollection(LazyCollectionOption.EXTRA)");

                            addOutImportType(ManyToMany.class.getName());
                            addOutImportType(JoinTable.class.getName());
                            addOutImportType(JoinColumn.class.getName());
                            addOutImportType(CascadeType.class.getName());
                            addOutImportType(FetchType.class.getName());

                            ls.getGetterAnnotations().add(saveUpdateCascade);

                            String joinTableName =
                                DigestUtils.md5Hex(ls.getColumnName());

                            String joinTableFormat =
                                "@JoinTable(name=\"JT%s\", "
                                    + "joinColumns=@JoinColumn(name=\"J%s\", referencedColumnName=\"jpaId\"), "
                                    + "inverseJoinColumns=@JoinColumn(name=\"IJ%s\", referencedColumnName=\"jpaId\"))";

                            String joinTable =
                                String.format(
                                    joinTableFormat,
                                    joinTableName,
                                    ls.getColumnName(),
                                    ls.getColumnName());

                            ls.getGetterAnnotations().add(
                                "@ManyToMany(fetch=FetchType.LAZY, "
                                    + eagerCascade + ")");
                            ls.getGetterAnnotations().add(joinTable);
                        }

                        // One-To-Many Bidirectional
                        else if (lsIsList && !rsIsList)
                        {
                            addOutImportType(LazyCollection.class.getName());
                            addOutImportType(LazyCollectionOption.class
                                .getName());
                            ls.getGetterAnnotations().add(
                                "@LazyCollection(LazyCollectionOption.EXTRA)");

                            ls.getGetterAnnotations().add(saveUpdateCascade);

                            addOutImportType(CascadeType.class.getName());
                            addOutImportType(OneToMany.class.getName());
                            addOutImportType(FetchType.class.getName());

                            String assocFormat =
                                "@OneToMany(fetch=FetchType.LAZY, mappedBy=\"%s\", %s)";
                            String assoc =
                                String
                                    .format(assocFormat, rsName, eagerCascade);
                            ls.getGetterAnnotations().add(assoc);
                        }

                        // Many-To-One Bidirectional
                        else if (!lsIsList && rsIsList)
                        {
                            ls.getGetterAnnotations().add(saveUpdateCascade);

                            addOutImportType(CascadeType.class.getName());
                            addOutImportType(ManyToOne.class.getName());
                            addOutImportType(JoinColumn.class.getName());
                            addOutImportType(FetchType.class.getName());

                            String joinColumnFormat =
                                "@JoinColumn(name=\"J%s\", referencedColumnName=\"jpaId\")";
                            String joinColumn =
                                String.format(joinColumnFormat, ls
                                    .getColumnName());

                            ls.getGetterAnnotations().add(
                                "@ManyToOne(fetch=FetchType.LAZY, "
                                    + eagerCascade + ")");
                            ls.getGetterAnnotations().add(joinColumn);
                        }

                        // One-To-One Bidirectional
                        else
                        {
                            ls.getGetterAnnotations().add(saveUpdateCascade);

                            addOutImportType(CascadeType.class.getName());
                            addOutImportType(OneToOne.class.getName());
                            addOutImportType(FetchType.class.getName());
                            addOutImportType(JoinColumn.class.getName());

                            String joinColumnFormat =
                                "@JoinColumn(name=\"J%s\", referencedColumnName=\"jpaId\")";
                            String joinColumn =
                                String.format(joinColumnFormat, ls
                                    .getColumnName());

                            ls.getGetterAnnotations().add(
                                "@OneToOne(fetch=FetchType.LAZY, "
                                    + eagerCascade + ")");
                            ls.getGetterAnnotations().add(joinColumn);
                        }
                    }
                }
                else if (mp == rs)
                {
                    boolean rsIsList = isList(rs.getOutType().getSimpleName());

                    // Many-To-Many Bidirectional
                    if (lsIsList && rsIsList)
                    {
                        addOutImportType(LazyCollection.class.getName());
                        addOutImportType(LazyCollectionOption.class.getName());
                        rs.getGetterAnnotations().add(
                            "@LazyCollection(LazyCollectionOption.EXTRA)");

                        rs.getGetterAnnotations().add(saveUpdateCascade);

                        addOutImportType(CascadeType.class.getName());
                        addOutImportType(ManyToMany.class.getName());
                        addOutImportType(FetchType.class.getName());
                        String assocFormat =
                            "@ManyToMany(fetch=FetchType.LAZY, mappedBy=\"%s\", %s)";
                        String assoc =
                            String.format(assocFormat, lsName, eagerCascade);
                        rs.getGetterAnnotations().add(assoc);
                    }

                    // Many-To-One Bidirectional
                    else if (lsIsList && !rsIsList)
                    {
                        rs.getGetterAnnotations().add(saveUpdateCascade);

                        addOutImportType(CascadeType.class.getName());
                        addOutImportType(ManyToOne.class.getName());
                        addOutImportType(JoinColumn.class.getName());
                        addOutImportType(FetchType.class.getName());

                        String joinColumnFormat =
                            "@JoinColumn(name=\"J%s\", referencedColumnName=\"jpaId\")";
                        String joinColumn =
                            String.format(joinColumnFormat, rs.getColumnName());

                        rs.getGetterAnnotations().add(
                            "@ManyToOne(fetch=FetchType.LAZY, " + eagerCascade
                                + ")");
                        rs.getGetterAnnotations().add(joinColumn);
                    }

                    // One-To-Many Bidirectional
                    else if (!lsIsList && rsIsList)
                    {
                        addOutImportType(LazyCollection.class.getName());
                        addOutImportType(LazyCollectionOption.class.getName());
                        rs.getGetterAnnotations().add(
                            "@LazyCollection(LazyCollectionOption.EXTRA)");

                        rs.getGetterAnnotations().add(saveUpdateCascade);

                        addOutImportType(CascadeType.class.getName());
                        addOutImportType(OneToMany.class.getName());
                        addOutImportType(FetchType.class.getName());
                        String assocFormat =
                            "@OneToMany(fetch=FetchType.LAZY, mappedBy=\"%s\", %s)";
                        String assoc =
                            String.format(assocFormat, lsName, eagerCascade);
                        rs.getGetterAnnotations().add(assoc);
                    }

                    // One-To-One Bidirectional
                    else
                    {
                        rs.getGetterAnnotations().add(saveUpdateCascade);

                        addOutImportType(CascadeType.class.getName());
                        addOutImportType(OneToOne.class.getName());
                        addOutImportType(FetchType.class.getName());
                        String assocFormat =
                            "@OneToOne(fetch=FetchType.LAZY, mappedBy=\"%s\", %s)";
                        String assoc =
                            String.format(assocFormat, lsName, eagerCascade);
                        rs.getGetterAnnotations().add(assoc);
                    }
                }
            }
        }
    }

    private boolean isMappedProperty(MetaProperty metaProp)
    {
        String propTypeCompName =
            getComponentType(metaProp.getOutType().getSimpleName());

        String propTypePkgName = metaProp.getOutType().getPackageName();

        return !propTypeCompName.equals("Object")
            && !isSimpleDataType(propTypeCompName)
            && !propTypePkgName.equals(Settings.getEnmPackageName())
            && !isTemporalType(propTypeCompName);
    }

    private void markAsCollOfElems(MetaProperty metaProp)
    {
        addOutImportType(CollectionOfElements.class.getName());
        metaProp.getGetterAnnotations().add("@CollectionOfElements");

        addOutImportType(JoinTable.class.getName());
        addOutImportType(JoinColumn.class.getName());

        // Import the column name and add it to the getter.
        String columnName =
            "@Column(name=\"" + metaProp.getColumnName() + "\")";
        addOutImportType(Column.class.getName());
        metaProp.getGetterAnnotations().add(columnName);

        String joinTableName = "JT" + metaProp.getColumnName();
        String joinColumnName = "J" + metaProp.getColumnName();

        String joinTableFormat =
            "@JoinTable(name=\"%s\", joinColumns=@JoinColumn(name=\"%s\"))";
        String joinTable =
            String.format(joinTableFormat, joinTableName, joinColumnName);

        metaProp.getGetterAnnotations().add(joinTable);
    }

    @Override
    public String getCompilationType()
    {
        return "class";
    }

    @Override
    public void writeCompilationTypeContent(Writer out) throws Exception
    {
        // Write the generated serial version UID.
        String serialUid =
            String.format(Constants.SERIAL_UID_PATT, UUID
                .randomUUID()
                .getLeastSignificantBits());
        out.write(serialUid);

        for (MetaProperty mp : getProperties())
        {
            // Get the property's name.
            String propName = mp.getName();

            // Get the simple name of the property's type.
            String propTypeSimpleName = mp.getOutType().getSimpleName();

            // Get the component name of the simple type name.
            String propTypeCompName = getComponentType(propTypeSimpleName);

            // Set the field name to be the same as the property name.
            String propFieldName = propName;

            // Up-case the first character of the property's name.
            String propNameUpCaseFirstChar = upCaseFirstChar(propName);

            // Determine whether or not the property type is java.lang.Object.
            boolean propTypeIsObject = isTypeObject(mp.getOutType());

            // Determine whether or not the property type is a List.
            boolean isPropTypeList = isList(mp.getOutType().getSimpleName());

            String fieldTypeName = propTypeSimpleName;

            if (propTypeIsObject && isPropTypeList)
            {
                out.write("    private List<Object> " + propFieldName
                    + " = new ArrayList<Object>();\n");
                out.write("    private SerializableObjectWrapper "
                    + propFieldName + "Wrapper;\n\n");

                String getPatt =
                    "    @SuppressWarnings(\"unchecked\")\n"
                        + "    @GenericType(Object.class)\n"
                        + "    @Transient\n"
                        + "    public List<Object> get%2$s()\n"
                        + "    {\n"
                        + "        if (this.%1$s != null)\n"
                        + "        {\n"
                        + "            return this.%1$s;\n"
                        + "        }\n"
                        + "        else if (this.%1$sWrapper == null)\n"
                        + "        {\n"
                        + "            return null;\n"
                        + "        }\n"
                        + "        else\n"
                        + "        {\n"
                        + "            this.%1$s = (List<Object>) this.%1$sWrapper.unwrap();\n"
                        + "            return this.%1$s;\n" + "        }\n"
                        + "    }\n\n";

                String getStr =
                    String.format(
                        getPatt,
                        propFieldName,
                        propNameUpCaseFirstChar);

                out.write(getStr);

                String setPatt =
                    "    public void set%2$s(List<Object> toSet)\n"
                        + "    {\n"
                        + "        this.%1$s = toSet;\n"
                        + "        this.%1$sWrapper = SerializableObjectWrapper.wrap(toSet);\n"
                        + "    }\n\n";

                String setStr =
                    String.format(
                        setPatt,
                        propFieldName,
                        propNameUpCaseFirstChar);

                out.write(setStr);

                String getWrapperPatt =
                    "    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)\n"
                        + "    @ManyToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.REFRESH})\n"
                        + "    @JoinColumn(name=\"J%3$s\", referencedColumnName=\"jpaId\")\n"
                        + "    @XmlTransient\n"
                        + "    public SerializableObjectWrapper get%2$sWrapper()\n"
                        + "    {\n"
                        + "        if (this.%1$sWrapper == null)\n"
                        + "        {\n"
                        + "            return null;\n"
                        + "        }\n"
                        + "        else\n"
                        + "        {\n"
                        + "            return this.%1$sWrapper;\n"
                        + "        }\n    }\n\n";

                String getWrapperStr =
                    String.format(
                        getWrapperPatt,
                        propFieldName,
                        propNameUpCaseFirstChar,
                        mp.getColumnName());

                out.write(getWrapperStr);

                String setWrapperPatt =
                    "    public void set%1$sWrapper(SerializableObjectWrapper toSet)\n"
                        + "    {\n" + "        this.%1$sWrapper = toSet;\n"
                        + "    }\n\n";

                String setWrapperStr =
                    String.format(setWrapperPatt, propFieldName);

                out.write(setWrapperStr);
            }
            else if (propTypeIsObject)
            {
                out.write("    private Object " + propFieldName + ";\n");
                out.write("    private SerializableObjectWrapper "
                    + propFieldName + "Wrapper;\n\n");

                String getPatt =
                    "    @Transient\n"
                        + "    public Object get%2$s()\n"
                        + "    {\n"
                        + "        if (this.%1$s != null)\n"
                        + "        {\n"
                        + "            return this.%1$s;\n"
                        + "        }\n"
                        + "        else if (this.%1$sWrapper == null)\n"
                        + "        {\n"
                        + "            return null;\n"
                        + "        }\n"
                        + "        else\n"
                        + "        {\n"
                        + "            this.%1$s = this.%1$sWrapper.unwrap();\n"
                        + "            return this.%1$s;\n" + "        }\n"
                        + "    }\n\n";

                String getStr =
                    String.format(
                        getPatt,
                        propFieldName,
                        propNameUpCaseFirstChar);

                out.write(getStr);

                String setPatt =
                    "    public void set%2$s(Object toSet)\n"
                        + "    {\n"
                        + "        this.%1$s = toSet;\n"
                        + "        this.%1$sWrapper = SerializableObjectWrapper.wrap(toSet);\n"
                        + "    }\n\n";

                String setStr =
                    String.format(
                        setPatt,
                        propFieldName,
                        propNameUpCaseFirstChar);

                out.write(setStr);

                String getWrapperPatt =
                    "    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)\n"
                        + "    @ManyToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.REFRESH})\n"
                        + "    @JoinColumn(name=\"J%3$s\", referencedColumnName=\"jpaId\")\n"
                        + "    @XmlTransient\n"
                        + "    public SerializableObjectWrapper get%2$sWrapper()\n"
                        + "    {\n"
                        + "        if (this.%1$sWrapper == null)\n"
                        + "        {\n"
                        + "            return null;\n"
                        + "        }\n"
                        + "        else\n"
                        + "        {\n"
                        + "            return this.%1$sWrapper;\n"
                        + "        }\n    }\n\n";

                String getWrapperStr =
                    String.format(
                        getWrapperPatt,
                        propFieldName,
                        propNameUpCaseFirstChar,
                        mp.getColumnName());

                out.write(getWrapperStr);

                String setWrapperPatt =
                    "    public void set%1$sWrapper(SerializableObjectWrapper toSet)\n"
                        + "    {\n" + "        this.%1$sWrapper = toSet;\n"
                        + "    }\n\n";

                String setWrapperStr =
                    String.format(setWrapperPatt, propFieldName);

                out.write(setWrapperStr);
            }
            else
            {
                // Write the field's annotations.
                for (String a : mp.getFieldAnnotations())
                {
                    out.write("    " + a + "\n");
                }

                // Determine which format and type name to use for the field.
                String fieldFormat = PROP_FLD_PATT;
                String initialzedFieldValue = "";

                if (isPropTypeList)
                {
                    initialzedFieldValue =
                        " = new ArrayList<" + propTypeCompName + ">()";
                }

                // Construct the field entry.
                String field =
                    String.format(
                        fieldFormat,
                        fieldTypeName,
                        propFieldName,
                        initialzedFieldValue);

                // Write the field entry.
                out.write(field);

                // Write the getter's annotations.
                for (String a : mp.getGetterAnnotations())
                {
                    out.write("    " + a + "\n");
                }

                // Construct the getter entry.
                String getter =
                    String.format(
                        PROP_GET_PATT,
                        propTypeSimpleName,
                        propNameUpCaseFirstChar,
                        propFieldName);

                // Write the getter entry.
                out.write(getter);

                // Construct the setter entry.
                String setter =
                    String.format(
                        PROP_SET_PATT,
                        propTypeSimpleName,
                        propNameUpCaseFirstChar,
                        propFieldName);

                // Writer the setter.
                out.write(setter);
            }

            if (!mp.equals(getProperties().get(getProperties().size() - 1)))
            {
                out.write("\n");
            }
        }

        out.write("\n");
    }
}
