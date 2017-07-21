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
package com.github.jonathanxd.codeapi.bytecode.processor.processors

import com.github.jonathanxd.codeapi.base.Access
import com.github.jonathanxd.codeapi.base.Scope
import com.github.jonathanxd.codeapi.base.ScopeAccess
import com.github.jonathanxd.codeapi.bytecode.processor.METHOD_VISITOR
import com.github.jonathanxd.codeapi.bytecode.processor.OUTER_TYPES_FIELDS
import com.github.jonathanxd.codeapi.bytecode.processor.TYPE_DECLARATION
import com.github.jonathanxd.codeapi.factory.accessField
import com.github.jonathanxd.codeapi.processor.Processor
import com.github.jonathanxd.codeapi.processor.ProcessorManager
import com.github.jonathanxd.codeapi.util.`is`
import com.github.jonathanxd.codeapi.util.require
import com.github.jonathanxd.iutils.data.TypedData
import org.objectweb.asm.Opcodes

object ScopeAccessProcessor : Processor<ScopeAccess> {

    override fun process(part: ScopeAccess, data: TypedData, processorManager: ProcessorManager<*>) {
        val type = TYPE_DECLARATION.require(data)
        val outer = OUTER_TYPES_FIELDS.require(data).first { it.typeDeclaration == type }

        when (part.scope) {
            Scope.OUTER -> {
                val field = outer.fields.first { it.type.`is`(part.type) }
                val access = accessField(type, Access.THIS, field.type, field.name)

                processorManager.process(access, data)
            }
            else -> {
            }
        }
    }

}