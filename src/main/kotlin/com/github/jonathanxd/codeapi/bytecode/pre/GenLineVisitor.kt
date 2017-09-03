/*
 *      CodeAPI-BytecodeWriter - Framework to generate Java code and Bytecode code. <https://github.com/JonathanxD/CodeAPI-BytecodeWriter>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.codeapi.bytecode.pre

import com.github.jonathanxd.codeapi.CodeInstruction
import com.github.jonathanxd.codeapi.CodePart
import com.github.jonathanxd.codeapi.CodeSource
import com.github.jonathanxd.codeapi.MutableCodeSource
import com.github.jonathanxd.codeapi.base.*
import com.github.jonathanxd.codeapi.bytecode.util.asmConstValue
import com.github.jonathanxd.codeapi.factory.line
import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.jwiutils.kt.typedKeyOf

/**
 * Visits lines incrementally.
 */
object GenLineVisitor {

    val CURRENT = typedKeyOf<Int>("CURRENT_LINE")

    @Suppress("UNCHECKED_CAST")
    fun <T : CodePart> visit(part: T): T = when (part) {
        is TypeDeclaration -> visit(part, TypedData())
        is FieldDeclaration -> visit(part, TypedData())
        is MethodDeclarationBase -> visit(part, TypedData())
        is CodeSource -> visit(part, TypedData())
        is CodeInstruction -> createLine(part, TypedData())
        else -> part
    } as T

    fun visit(typeDeclaration: TypeDeclaration, data: TypedData): TypeDeclaration {
        return typeDeclaration.builder()
                .methods(typeDeclaration.methods.map { visit(it, data) })
                .innerTypes(typeDeclaration.innerTypes.map { visit(it, data) })
                .fields(typeDeclaration.fields.map { visit(it, data) })
                .let {
                    if (it is ConstructorsHolder.Builder<*, *> && typeDeclaration is ConstructorsHolder) {
                        it.constructors(typeDeclaration.constructors.map { visit(it, data) })
                                as TypeDeclaration.Builder<TypeDeclaration, *>
                    } else it
                }
                .build()
    }

    fun visit(fieldDeclaration: FieldDeclaration, data: TypedData): FieldDeclaration =
            if (fieldDeclaration.value.asmConstValue == null)
                fieldDeclaration.builder()
                        .value(createLineInstance(fieldDeclaration.value, data))
                        .innerTypes(fieldDeclaration.innerTypes.map { visit(it, data) })
                        .build()
            else
                fieldDeclaration

    @Suppress("UNCHECKED_CAST")
    fun <T : MethodDeclarationBase> visit(methodDeclarationBase: T, data: TypedData): T =
            methodDeclarationBase.builder()
                    .body(visit(methodDeclarationBase.body, data))
                    .innerTypes(methodDeclarationBase.innerTypes.map { visit(it, data) })
                    .build() as T


    fun visit(codeSource: CodeSource, data: TypedData): CodeSource {
        return codeSource.map { createLine(it, data) }.let {
            if (codeSource is MutableCodeSource) MutableCodeSource.create(it)
            else CodeSource.fromIterable(it)
        }
    }

    // TODO: Fix, lines are calculated inverted because createLineInstance(..) runs after visit inside of it
    fun createLine(insn: CodeInstruction, data: TypedData): CodeInstruction = when (insn) {
        is IfStatement -> createLineAndTransform(insn, data) {
            it.builder()
                    .body(visit(insn.body, data))
                    .elseStatement(visit(insn.elseStatement, data))
                    .build()
        }
        is TryStatementBase -> createLineAndTransform(insn, data) {
            it.builder()
                    .body(visit(insn.body, data))
                    .catchStatements(insn.catchStatements.map { it.builder().body(visit(it.body, data)).build() })
                    .finallyStatement(visit(insn.finallyStatement, data))
                    .build()
        }
        is SwitchStatement -> createLineAndTransform(insn, data) {
            it.builder()
                    .cases(insn.cases.map { it.builder().body(visit(it.body, data)).build() })
                    .build()
        }
        is LocalCode -> createLineAndTransform(insn, data) {
            it.builder()
                    .declaration(visit(insn.declaration, data))
                    .build()
        }
        is BodyHolder -> createLineAndTransform(insn, data) {
            (it as BodyHolder).builder()
                    .body(visit(insn.body, data))
                    .build() as CodeInstruction
        }
        else -> createLineInstance(insn, data)
    }

    private fun <T : CodeInstruction> createLineAndTransform(insn: T, data: TypedData, transformer: (T) -> T): Line {
        val line = getLine(data)
        return line(line, transformer(insn))
    }

    private fun createLineInstance(insn: CodeInstruction, data: TypedData): Line {
        return line(getLine(data), insn)
    }

    private fun getLine(data: TypedData): Int {
        return (CURRENT.getOrSet(data, -1) + 1).also { CURRENT.set(data, it) }
    }

}