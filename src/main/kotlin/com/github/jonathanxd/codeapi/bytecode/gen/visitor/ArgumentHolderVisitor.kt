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
package com.github.jonathanxd.codeapi.bytecode.gen.visitor

import com.github.jonathanxd.codeapi.CodeAPI
import com.github.jonathanxd.codeapi.base.ArgumentHolder
import com.github.jonathanxd.codeapi.bytecode.BytecodeClass
import com.github.jonathanxd.codeapi.bytecode.common.MVData
import com.github.jonathanxd.codeapi.bytecode.util.CodePartUtil
import com.github.jonathanxd.codeapi.bytecode.util.InsnUtil
import com.github.jonathanxd.codeapi.common.CodeArgument
import com.github.jonathanxd.codeapi.gen.visit.VisitorGenerator
import com.github.jonathanxd.codeapi.gen.visit.VoidVisitor
import com.github.jonathanxd.iutils.data.MapData

object ArgumentHolderVisitor : VoidVisitor<ArgumentHolder, BytecodeClass, MVData> {

    override fun voidVisit(t: ArgumentHolder, extraData: MapData, visitorGenerator: VisitorGenerator<BytecodeClass>, additional: MVData) {
        val mv = additional.methodVisitor

        val types = t.types
        // Try to auto box and unbox
        val arguments = t.arguments.mapIndexed { i, it ->
            val value = it.value
            val type = CodePartUtil.getTypeOrNull(value)
            val argType = types[i]

            if (type != null) {
                if (type.isPrimitive && !argType.isPrimitive) {
                    return@mapIndexed CodeArgument(CodeAPI.cast(type, argType, value))
                } else if (!type.isPrimitive && argType.isPrimitive) {
                    return@mapIndexed CodeArgument(CodeAPI.cast(type, argType, value))
                }
            }

            return@mapIndexed it
        }

        if (!t.array) {

            for (argument in arguments) {
                val value = argument.value

                visitorGenerator.generateTo(value.javaClass, value, extraData, null, additional)

            }
        } else {
            for (i in arguments.indices) {

                InsnUtil.visitInt(i, mv) // Visit index

                val argument = arguments[i]

                val value = argument.value

                visitorGenerator.generateTo(value.javaClass, value, extraData, null, additional)

            }
        }
    }

}