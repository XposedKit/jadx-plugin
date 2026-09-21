package cc.meteormc.xposedkit.jadx.generator

import cc.meteormc.xposedkit.jadx.data.MatchMode
import jadx.core.dex.nodes.FieldNode
import java.util.EnumSet

object FieldReflectGenerator : BaseGenerator<FieldNode>() {
    override fun generate(
        node : FieldNode,
        mode: MatchMode,
        useLambda: Boolean
    ): String {
        val clazz = node.declaringClass
        val codeBuilder = StringBuilder()
        if (mode == MatchMode.FEATURE) {
            val fields = clazz.fields
            val accFlags = node.accessFlags.rawValue()
            when {
                fields.noneNode(node) { it.type typeEquals node.type } -> {
                    codeBuilder.append("fields(${node.type.toReference()}).first()")
                }
                fields.noneNode(node) { it.accessFlags.rawValue() == accFlags } -> {
                    codeBuilder.append("declaredFields.first { it.modifiers == ${accFlags.toModifiers()} }")
                }
                fields.noneNode(node) {
                    it.type typeEquals node.type && it.accessFlags.rawValue() == accFlags
                } -> {
                    codeBuilder.append("declaredFields.first { it.type == ${node.type.toReference()} && it.modifiers == ${accFlags.toModifiers()} }")
                }
            }
        }

        if (codeBuilder.isBlank()) {
            codeBuilder.append("field(${node.name.quote()})")
        }

        return clazz.rawName().createSnippet(codeBuilder.toString(), useLambda)
    }

    override fun getPreferredMode(node: FieldNode): MatchMode {
        if (node.isObfuscated()) {
            return MatchMode.FEATURE
        }

        return MatchMode.STANDARD
    }

    override fun getDisabledModes(node: FieldNode): EnumSet<MatchMode> {
        return EnumSet.of(MatchMode.EXACT)
    }
}