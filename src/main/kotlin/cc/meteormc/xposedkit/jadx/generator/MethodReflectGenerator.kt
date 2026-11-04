package cc.meteormc.xposedkit.jadx.generator

import cc.meteormc.xposedkit.jadx.data.MatchMode
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.nodes.MethodNode
import java.util.EnumSet

object MethodReflectGenerator : BaseGenerator<MethodNode>() {
    override fun generate(
        node: MethodNode,
        mode: MatchMode,
        useLambda: Boolean
    ): String {
        val clazz = node.declaringClass
        val isConstructor = node.isConstructor

        val rawName = clazz.rawName()
        val constructors = clazz.methods.filter { it.isConstructor }
        val methods = clazz.methods.filterNot { it.isConstructor }

        val prefix: String
        var withName: Boolean
        var withParams: Boolean
        if (isConstructor) {
            prefix = "constructor"
            withName = false
            withParams = true
        } else {
            prefix = "method"
            withName = true
            withParams = mode == MatchMode.EXACT || shouldUseExact(node)
        }

        if (mode == MatchMode.FEATURE) {
            if (isConstructor && constructors.singleOrNull() == node) {
                return rawName.createSnippet(
                    "declaredConstructors.single()",
                    useLambda
                )
            } else {
                val target = if (isConstructor) constructors else methods
                val features = target.findUnique(node, *buildFeatures(node))
                if (features.isNotEmpty()) {
                    return rawName.createSnippet(
                        "$prefix {\n${features.joinToString("\n").prependIndent("    ")}\n}",
                        useLambda
                    )
                }
            }
        }

        return rawName.createSnippet(
            node.internalGenerate(prefix, withName, withParams),
            useLambda
        )
    }

    private fun buildFeatures(node: MethodNode): Array<FeatureInfo<MethodNode>> {
        val features = mutableListOf<FeatureInfo<MethodNode>>()
        features.add(
            FeatureInfo("paramTypes = ${node.argTypes.toReference()}") {
                it.argTypes.zip(node.argTypes).all { (actual, expected) -> actual typeEquals expected }
            }
        )
        features.add(
            FeatureInfo("paramCount = ${node.argTypes.size}") {
                it.argTypes.size == node.argTypes.size
            }
        )
        if (!node.isConstructor) {
            features.add(
                FeatureInfo("returnType = ${node.returnType.toReference()}") {
                    it.returnType typeEquals node.returnType
                }
            )
            features.add(
                FeatureInfo("isBridge = ${node.accessFlags.isBridge}") {
                    it.accessFlags.isBridge == node.accessFlags.isBridge
                }
            )
        }
        features.add(
            FeatureInfo("modifiers = ${node.accessFlags.rawValue().toModifiers()}") {
                it.accessFlags.rawValue() == node.accessFlags.rawValue()
            }
        )
        features.add(
            FeatureInfo("isSynthetic = ${node.contains(AFlag.SYNTHETIC)}") {
                it.contains(AFlag.SYNTHETIC) == node.contains(AFlag.SYNTHETIC)
            }
        )
        return features.toTypedArray()
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