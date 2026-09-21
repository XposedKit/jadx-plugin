package cc.meteormc.xposedkit.jadx

import cc.meteormc.xposedkit.jadx.generator.ClassReflectGenerator
import cc.meteormc.xposedkit.jadx.generator.FieldReflectGenerator
import cc.meteormc.xposedkit.jadx.generator.MethodReflectGenerator
import cc.meteormc.xposedkit.jadx.ui.AdvancedDialog
import cc.meteormc.xposedkit.jadx.util.I18n
import jadx.api.plugins.JadxPlugin
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.JadxPluginInfo
import jadx.api.plugins.JadxPluginInfoBuilder
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.FieldNode
import jadx.core.dex.nodes.ICodeNode
import jadx.core.dex.nodes.MethodNode
import jadx.gui.plugins.context.GuiPluginContext

class JadxPlugin : JadxPlugin {
    companion object {
        const val PLUGIN_ID = "xposedkit-extension"
    }

    override fun getPluginInfo(): JadxPluginInfo = JadxPluginInfoBuilder
        .pluginId(PLUGIN_ID)
        .name(I18n.str("plugin.name"))
        .description(I18n.str("plugin.description"))
        .homepage("https://github.com/XposedKit/XposedKit")
        .build()

    override fun init(context: JadxPluginContext) {
        val gui = context.guiContext as? GuiPluginContext? ?: return
        context.registerOptions(PluginOptions)
        gui.addPopupMenuAction(
            I18n.str("popup.copy-reflect"),
            { it is ICodeNode },
            PluginOptions.copyReflectShortcut
        ) { node ->
            if (node !is ICodeNode) {
                return@addPopupMenuAction
            }

            if (PluginOptions.advancedMode) {
                AdvancedDialog(node, gui).show()
                return@addPopupMenuAction
            }

            val code = when (node) {
                is ClassNode -> ClassReflectGenerator.generate(node)
                is MethodNode -> MethodReflectGenerator.generate(node)
                is FieldNode -> FieldReflectGenerator.generate(node)
                else -> return@addPopupMenuAction
            }
            gui.copyToClipboard(code)
        }
    }
}