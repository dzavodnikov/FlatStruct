/*
 *     Copyright 2025 The FlatStruct Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flatstruct;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtNewMethod;

/**
 * Helper class that simplify generation new methods.
 */
public class MethodBuilder {

    private final List<String> modifiers = new ArrayList<String>();

    private String returnType = "void";

    private final String name;

    private final List<String> arguments = new ArrayList<String>();

    private final List<String> body = new ArrayList<String>();

    public MethodBuilder(final String name) {
        this.name = name;
    }

    public void addModifier(final String modifier) {
        this.modifiers.add(modifier);
    }

    public void setReturnType(final Class<?> returnType) {
        this.returnType = returnType.getName();
    }

    public void addArgument(final Class<?> argType, final String argName) {
        this.arguments.add(String.format("%s %s", argType.getName(), argName));
    }

    public void addArgument(final Parameter parameter) {
        addArgument(parameter.getType(), parameter.getName());
    }

    public void addBodyLine(final String line, final Object... vars) {
        final String fmtLine = String.format(line, vars);
        this.body.add(String.format("    %s", fmtLine));
    }

    protected String buildMethod() {
        final List<String> all = new ArrayList<>();
        all.add(String.join(" ", this.modifiers));
        all.add(" ");
        all.add(this.returnType);
        all.add(" ");
        all.add(this.name);
        all.add("(");
        all.add(String.join(", ", this.arguments));
        all.add(") {\n");
        all.add(String.join("\n", this.body));
        all.add("\n}");
        return String.join("", all);
    }

    public void addMethodTo(final CtClass ctClass) throws CannotCompileException {
        ctClass.addMethod(CtNewMethod.make(buildMethod(), ctClass));
    }
}
