package cc.meteormc.xposedkit.jadx.generator

import cc.meteormc.xposedkit.jadx.data.MatchMode
import jadx.core.dex.attributes.AFlag
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
            if (fields.noneNode(node) { it.type typeEquals node.type }) {
                codeBuilder.append("field(${node.type.toReference()})")
            } else {
                val features = fields.findUnique(
                    node,
                    FeatureInfo("type = ${node.type.toReference()}") {
                        it.type typeEquals node.type
                    },
                    FeatureInfo("modifiers = ${node.accessFlags.rawValue().toModifiers()}") {
                        it.accessFlags.rawValue() == node.accessFlags.rawValue()
                    },
                    FeatureInfo("isSynthetic = ${node.contains(AFlag.SYNTHETIC)}") {
                        it.contains(AFlag.SYNTHETIC) == node.contains(AFlag.SYNTHETIC)
                    }
                )
                if (features.isNotEmpty()) {
                    codeBuilder.append(
                        """
                        field {
                            ${features.joinToString("\n")}
                        }
                        """.trimIndent()
                    )
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