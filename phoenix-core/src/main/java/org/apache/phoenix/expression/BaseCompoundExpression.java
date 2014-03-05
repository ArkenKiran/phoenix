/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.phoenix.expression;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.WritableUtils;

import com.google.common.collect.ImmutableList;
import org.apache.phoenix.expression.visitor.ExpressionVisitor;


public abstract class BaseCompoundExpression extends BaseExpression {
    protected List<Expression> children;
    private boolean isNullable;
    private boolean isStateless;
    private boolean isDeterministic;
   
    public BaseCompoundExpression() {
    }
    
    public BaseCompoundExpression(List<Expression> children) {
        init(children);
    }
    
    private void init(List<Expression> children) {
        this.children = ImmutableList.copyOf(children);
        boolean isStateless = true;
        boolean isDeterministic = true;
        boolean isNullable = false;
        for (int i = 0; i < children.size(); i++) {
            Expression child = children.get(i);
            isNullable |= child.isNullable();
            isStateless &= child.isStateless();
            isDeterministic &= child.isDeterministic();
        }
        this.isStateless = isStateless;
        this.isDeterministic = isDeterministic;
        this.isNullable = isNullable;
    }
    
    @Override
    public List<Expression> getChildren() {
        return children;
    }
    
    
    @Override
    public boolean isDeterministic() {
        return isDeterministic;
    }
    
    @Override
    public boolean isStateless() {
        return isStateless;
    }

    @Override
    public boolean isNullable() {
        return isNullable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + children.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BaseCompoundExpression other = (BaseCompoundExpression)obj;
        if (!children.equals(other.children)) return false;
        return true;
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        int len = WritableUtils.readVInt(input);
        List<Expression>children = new ArrayList<Expression>(len);
        for (int i = 0; i < len; i++) {
            Expression child = ExpressionType.values()[WritableUtils.readVInt(input)].newInstance();
            child.readFields(input);
            children.add(child);
        }
        init(children);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        WritableUtils.writeVInt(output, children.size());
        for (int i = 0; i < children.size(); i++) {
            Expression child = children.get(i);
            WritableUtils.writeVInt(output, ExpressionType.valueOf(child).ordinal());
            child.write(output);
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).reset();
        }
    }
    
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        List<T> l = acceptChildren(visitor, visitor.visitEnter(this));
        T t = visitor.visitLeave(this, l);
        if (t == null) {
            t = visitor.defaultReturn(this, l);
        }
        return t;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + " [children=" + children + "]";
    }
}
