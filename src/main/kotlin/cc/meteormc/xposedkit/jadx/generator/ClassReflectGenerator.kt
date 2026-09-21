package cc.meteormc.xposedkit.jadx.generator

import cc.meteormc.xposedkit.jadx.data.MatchMode
import jadx.core.dex.nodes.ClassNode
import java.util.EnumSet

object ClassReflectGenerator : BaseGenerator<ClassNode>() {
    override fun generate(
        node: ClassNode,
        mode: MatchMode,
        useLambda: Boolean
    ): String {
        return node.rawName().createSnippet("", useLambda)
    }

    override fun getPreferredMode(node: ClassNode): MatchMode {
        return MatchMode.STANDARD
    }

    override fun getDisabledModes(node: ClassNode): EnumSet<MatchMode> {
        return EnumSet.of(
            MatchMode.EXACT,
            MatchMode.FEATURE
        )
    }
}