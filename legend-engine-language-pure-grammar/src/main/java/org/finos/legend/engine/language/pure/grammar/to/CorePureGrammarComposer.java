// Copyright 2020 Goldman Sachs
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

package org.finos.legend.engine.language.pure.grammar.to;

import org.eclipse.collections.api.block.function.Function2;
import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.utility.LazyIterate;
import org.eclipse.collections.impl.utility.ListIterate;
import org.finos.legend.engine.language.pure.grammar.to.extension.PureGrammarComposerExtension;
import org.finos.legend.engine.protocol.pure.v1.model.data.EmbeddedData;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.PackageableElement;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.data.DataElement;

import java.util.List;

public class CorePureGrammarComposer implements PureGrammarComposerExtension
{
    @Override
    public List<Function3<List<PackageableElement>, PureGrammarComposerContext, String, String>> getExtraSectionComposers()
    {
        return Lists.mutable.with((elements, context, sectionName) ->
        {
            if ("Data".equals(sectionName))
            {
                return ListIterate.collect(elements, element ->
                {
                    if (element instanceof DataElement)
                    {
                        return renderDataElement((DataElement) element, context);
                    }
                    return "/* Can't transform element '" + element.getPath() + "' in this section */";
                }).makeString("\n\n");
            }
            else
            {
                return null;
            }
        });
    }

    @Override
    public List<Function3<List<PackageableElement>, PureGrammarComposerContext, List<String>, PureFreeSectionGrammarComposerResult>> getExtraFreeSectionComposers()
    {
        return Lists.mutable.with((elements, context, composedSections) ->
        {
            List<DataElement> composableElements = ListIterate.selectInstancesOf(elements, DataElement.class);
            return composableElements.isEmpty() ? null : new PureFreeSectionGrammarComposerResult(LazyIterate.collect(composableElements, e -> renderDataElement(e, context)).makeString("###Data" + "\n", "\n\n", ""), composableElements);
        });
    }

    @Override
    public List<Function2<EmbeddedData, PureGrammarComposerContext, String>> getExtraEmbeddedDataComposers()
    {
        return Lists.mutable.with(HelperEmbeddedDataGrammarComposer::composeCoreEmbeddedDataTypes);
    }

    public static String renderDataElement(DataElement dataElement, PureGrammarComposerContext context)
    {
        return "Data " + HelperDomainGrammarComposer.renderAnnotations(dataElement.stereotypes, dataElement.taggedValues) + PureGrammarComposerUtility.convertPath(dataElement.getPath()) + "\n" +
                HelperEmbeddedDataGrammarComposer.composeEmbeddedData(dataElement.data, context);
    }
}
