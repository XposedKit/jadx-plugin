package cc.meteormc.xposedkit.jadx.generator

import cc.meteormc.xposedkit.jadx.data.MatchMode
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.nodes.MethodNode
import java.util.EnumSet

object MethodReflectGenerator : BaseGenerator<MethodNode>() {
    override fun generate(
        node: MethodNode,
        mode: MatchMode,
        useLambda: Boolean
    ): String {
        val params = node.argTypes
        val clazz = node.declaringClass

        val methods = clazz.methods
        val constructors = methods.filter { it.isConstructor }
        val rawName = clazz.rawName()
        if (mode == MatchMode.FEATURE) {
            if (node.isConstructor) {
                if (constructors.singleOrNull() == node) {
                    return rawName.createSnippet(
                        "declaredConstructors.single()",
                        useLambda
                    )
                }
            } else {
                when {
                    methods.noneNode(node) { it.returnType typeEquals node.returnType } -> {
                        return rawName.createSnippet(
                            "declaredMethods.first { it.returnType == ${node.returnType.toReference()} }",
                            useLambda
                        )
                    }
                    methods.noneNode(node) { it.argTypes == params } -> {
                        val code = node.internalGenerate(prefix = "method", withName = false, withParams = true)
                        return rawName.createSnippet(code, useLambda)
                    }
                    methods.noneNode(node) { it.argTypes.size == params.size } -> {
                        return rawName.createSnippet(
                            "declaredMethods.first { it.parameterCount == ${params.size} }",
                            useLambda
                        )
                    }
                    methods.noneNode(node) {
                        it.returnType typeEquals node.returnType && it.argTypes == params
                    } -> {
                        val code = node.internalGenerate(prefix = "methods", withName = false, withParams = true)
                        return rawName.createSnippet(
                            "$code.first {\n    it.returnType == ${node.returnType.toReference()}\n}",
                            useLambda
                        )
                    }
                    methods.noneNode(node) {
                        it.returnType typeEquals node.returnType && it.argTypes.size == params.size
                    } -> {
                        return rawName.createSnippet(
                            "declaredMethods.first { it.returnType == ${node.returnType.toReference()} && it.parameterCount == ${params.size} }",
                            useLambda
                        )
                    }
                }
            }
        }

        val prefix: String
        var withName: Boolean
        var withParams: Boolean
        if (node.isConstructor) {
            prefix = "constructor"
            withName = false
            withParams = true
        } else {
            prefix = "method"
            withName = true
            withParams = mode == MatchMode.EXACT || shouldUseExact(node)
        }

        return rawName.createSnippet(
            node.internalGenerate(prefix, withName, withParams),
            useLambda
        )
    }

    private fun MethodNode.internalGenerate(prefix: String, withName: Boolean, withParams: Boolean): String {
        val withParams = withParams && argTypes.isNotEmpty()
        return buildString {
            append(prefix)
            append("(")
            if (withParams) append("\n")
            if (withName) {
                if (withParams) append(" ".repeat(4))
                append(name.quote())
                if (withParams) append(",\n")
            }
            if (withParams) {
                append(buildParams(argTypes).prependIndent(" ".repeat(4)))
                append("\n")
            }
            append(")")
        }
    }

    override fun getPreferredMode(node: MethodNode): MatchMode {
        if (node.isObfuscated()) {
            return MatchMode.FEATURE
        }

        if (node.isConstructor || shouldUseExact(node)) {
            return MatchMode.EXACT
        }

        return MatchMode.STANDARD
    }

    override fun getDisabledModes(node: MethodNode): EnumSet<MatchMode> {
        if (node.isConstructor || shouldUseExact(node)) {
            return EnumSet.of(MatchMode.STANDARD)
        }

        return EnumSet.noneOf(MatchMode::class.java)
    }

    private fun buildParams(params: List<ArgType>): String {
        return params.joinToString(",\n") {
            it.toReference()
        }
    }

    private fun shouldUseExact(node: MethodNode): Boolean {
        val name = node.name
        val params = node.argTypes
        val clazz = node.declaringClass
        val methods = clazz.methods.toMutableList()
        clazz.visitParentClasses {
            methods.addAll(it.methods)
        }

        // 如果存在同名的且非重写的重载方法，则视为应当使用精确模式匹配
        return params.isNotEmpty() && methods.any { it != node && it.name == name && it.argTypes != params }
    }
}