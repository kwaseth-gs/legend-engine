// Copyright 2022 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.language.pure.compiler.test;

import org.eclipse.collections.api.tuple.Pair;
import org.finos.legend.engine.language.pure.compiler.toPureGraph.PureModel;
import org.finos.legend.engine.protocol.pure.v1.model.context.PureModelContextData;
import org.finos.legend.pure.generated.Root_meta_data_BinaryData;
import org.finos.legend.pure.generated.Root_meta_data_DataElement;
import org.finos.legend.pure.generated.Root_meta_data_PureCollectionData;
import org.finos.legend.pure.generated.Root_meta_data_TextData;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.PackageableElement;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.InstanceValue;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel.valuespecification.SimpleFunctionExpression;
import org.junit.Assert;
import org.junit.Test;

public class TestDataCompilationFromGrammar extends TestCompilationFromGrammar.TestCompilationFromGrammarTestSuite
{
    @Override
    protected String getDuplicatedElementTestCode()
    {
        return "Class model::element {}\n" +
                "###Data\n" +
                "Data model::element\n" +
                "Text #{\n" +
                "  contentType: 'application/x.flatdata';\n" +
                "  data: 'sample data';\n" +
                "}#\n";
    }

    @Override
    protected String getDuplicatedElementTestExpectedErrorMessage()
    {
        return "COMPILATION error at [3:1-7:2]: Duplicated element 'model::element'";
    }

    @Test
    public void testCannotUseReferenceInADataElement()
    {
        test("###Data\n" +
                        "Data <<meta::pure::profiles::typemodifiers.abstract>> {doc.doc = 'something'} meta::data::MyData\n" +
                        "Reference #{ meta::data::MyData }#\n",
                "COMPILATION error at [3:1-34]: Cannot use Data element reference in a Data element"
        );
    }

    @Test
    public void testTextDataCompilation()
    {
        Pair<PureModelContextData, PureModel> result = test("###Data\n" +
                "Data <<meta::pure::profiles::typemodifiers.abstract>> {doc.doc = 'something'} meta::data::MyData\n" +
                "Text #{\n" +
                "  contentType: 'application/json';\n" +
                "  data: '{\"some\":\"data\"}';\n" +
                "}#\n"
        );
        PackageableElement element = result.getTwo().getPackageableElement("meta::data::MyData");
        Assert.assertTrue(element instanceof Root_meta_data_DataElement);
        Root_meta_data_DataElement dataElement = (Root_meta_data_DataElement) element;

        Assert.assertTrue(dataElement._data() instanceof Root_meta_data_TextData);
        Root_meta_data_TextData textData = (Root_meta_data_TextData) dataElement._data();

        Assert.assertEquals("application/json", textData._contentType());
        Assert.assertEquals("{\"some\":\"data\"}", textData._data());
    }

    @Test
    public void testBinaryDataCompilation()
    {
        Pair<PureModelContextData, PureModel> result = test("###Data\n" +
                "Data <<meta::pure::profiles::typemodifiers.abstract>> {doc.doc = 'something'} meta::data::MyData\n" +
                "Binary #{\n" +
                "  contentType: 'application/x-protobuf';\n" +
                "  data: '1B4A 9DEA 230F FF20';\n" +
                "}#\n"
        );
        PackageableElement element = result.getTwo().getPackageableElement("meta::data::MyData");
        Assert.assertTrue(element instanceof Root_meta_data_DataElement);
        Root_meta_data_DataElement dataElement = (Root_meta_data_DataElement) element;

        Assert.assertTrue(dataElement._data() instanceof Root_meta_data_BinaryData);
        Root_meta_data_BinaryData binaryData = (Root_meta_data_BinaryData) dataElement._data();

        Assert.assertEquals("application/x-protobuf", binaryData._contentType());
        Assert.assertEquals("1B4A9DEA230FFF20", binaryData._hexData());
    }

    @Test
    public void testPureCollectionDataCompilation()
    {
        Pair<PureModelContextData, PureModel> result = test("###Pure\n" +
                "Enum enums::Gender\n" +
                "{\n" +
                "  MALE, FEMALE, OTHER\n" +
                "}\n" +
                "Class my::Address\n" +
                "{\n" +
                "  street : String[1];\n" +
                "}\n" +
                "Class my::Person\n" +
                "{\n" +
                "  givenNames  : String[*];\n" +
                "  lastName    : String[1];\n" +
                "  dateOfBirth : StrictDate[1];\n" +
                "  timeOfBirth : StrictTime[1];\n" +
                "  timeOfDeath : DateTime[0..1];\n" +
                "  isAlive     : Boolean[1];\n" +
                "  height      : Float[1];\n" +
                "  girth       : Decimal[1];\n" +
                "  shoeSize    : Integer[1];\n" +
                "  score1      : Integer[1];\n" +
                "  score2      : Float[1];\n" +
                "  score3      : Decimal[1];\n" +
                "  gender      : enums::Gender[1];\n" +
                "  address     : my::Address[1];\n" +
                "}\n" +
                "\n" +
                "###Data\n" +
                "Data <<meta::pure::profiles::typemodifiers.abstract>> {doc.doc = 'something'} meta::data::MyData\n" +
                "PureCollection #{\n" +
                "  data: [\n" +
                "    ^my::Person(\n" +
                "      givenNames  = ['Fred', 'William'],\n" +
                "      lastName    = 'Bloggs',\n" +
                "      dateOfBirth = %2001-03-12,\n" +
                "      timeOfBirth = %12:23,\n" +
                "      timeOfDeath = %2020-09-11T12:56:24.487,\n" +
                "      isAlive     = false,\n" +
                "      height      = 1.76,\n" +
                "      girth       = 0.98D,\n" +
                "      shoeSize    = 10,\n" +
                "      score1      = -1,\n" +
                "      score2      = -1.3,\n" +
                "      score3      = -1.8D,\n" +
                "      gender      = enums::Gender.MALE,\n" +
                "      address     = ^my::Address(street = 'A Road')\n" +
                "    ),\n" +
                "    ^my::Person(\n" +
                "      givenNames  = 'Jane',\n" +
                "      lastName    = 'Doe',\n" +
                "      dateOfBirth = %1984-04-22,\n" +
                "      timeOfBirth = %19:41:21,\n" +
                "      isAlive     = true,\n" +
                "      height      = 1.61,\n" +
                "      girth       = 0.81D,\n" +
                "      shoeSize    = 4,\n" +
                "      score1      = -1,\n" +
                "      score2      = -1.3,\n" +
                "      score3      = -1.8D,\n" +
                "      gender      = enums::Gender.FEMALE,\n" +
                "      address     = ^my::Address(street = 'B Road')\n" +
                "    )\n" +
                "  ];\n" +
                "}#\n"
        );
        PackageableElement element = result.getTwo().getPackageableElement("meta::data::MyData");
        Assert.assertTrue(element instanceof Root_meta_data_DataElement);
        Root_meta_data_DataElement dataElement = (Root_meta_data_DataElement) element;

        Assert.assertTrue(dataElement._data() instanceof Root_meta_data_PureCollectionData);
        Root_meta_data_PureCollectionData pureCollectionData = (Root_meta_data_PureCollectionData) dataElement._data();

        InstanceValue collection = pureCollectionData._collection();
        Assert.assertEquals(2, collection._values().size());
        Assert.assertTrue(collection._values().allSatisfy(SimpleFunctionExpression.class::isInstance));
    }
}
