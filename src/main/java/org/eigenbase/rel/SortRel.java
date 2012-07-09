/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.rel;

import java.util.List;

import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.*;


/**
 * Relational expression which imposes a particular sort order on its input
 * without otherwise changing its content.
 */
public class SortRel
    extends SingleRel
{
    //~ Instance fields --------------------------------------------------------

    protected final List<RelFieldCollation> collations;
    protected final RexNode [] fieldExps;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a sorter.
     *
     * @param cluster Cluster this relational expression belongs to
     * @param traits Traits
     * @param child input relational expression
     * @param collations array of sort specifications
     */
    public SortRel(
        RelOptCluster cluster,
        RelTraitSet traits,
        RelNode child,
        List<RelFieldCollation> collations)
    {
        super(cluster, traits, child);
        this.collations = collations;

        fieldExps = new RexNode[collations.size()];
        final RelDataTypeField [] fields = getRowType().getFields();
        for (int i = 0; i < collations.size(); ++i) {
            int iField = collations.get(i).getFieldIndex();
            fieldExps[i] =
                cluster.getRexBuilder().makeInputRef(
                    fields[iField].getType(),
                    iField);
        }
    }

    //~ Methods ----------------------------------------------------------------

    public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
        assert traitSet.comprises(CallingConvention.NONE);
        return new SortRel(
            getCluster(),
            getCluster().traitSetOf(CallingConvention.NONE),
            sole(inputs),
            collations);
    }

    public RexNode [] getChildExps()
    {
        return fieldExps;
    }

    /**
     * @return array of RelFieldCollations, from most significant to least
     * significant
     */
    public List<RelFieldCollation> getCollations()
    {
        return collations;
    }

    public void explain(RelOptPlanWriter pw)
    {
        String [] terms = new String[1 + (collations.size() * 2)];
        Object [] values = new Object[collations.size()];
        int i = 0;
        terms[i++] = "child";
        for (int j = 0; j < collations.size(); ++j) {
            terms[i++] = "sort" + j;
        }
        for (int j = 0; j < collations.size(); ++j) {
            terms[i++] = "dir" + j;
            values[j] = collations.get(j).getDirection();
        }
        pw.explain(this, terms, values);
    }
}

// End SortRel.java