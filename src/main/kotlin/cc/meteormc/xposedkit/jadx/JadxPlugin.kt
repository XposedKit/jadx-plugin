package cc.meteormc.xposedkit.jadx

import cc.meteormc.xposedkit.jadx.ui.AdvancedDialog
import cc.meteormc.xposedkit.jadx.util.I18n
import cc.meteormc.xposedkit.jadx.util.KeyboardManager
import jadx.api.plugins.JadxPlugin
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.JadxPluginInfo
import jadx.api.plugins.JadxPluginInfoBuilder
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
            { ReflectCodeGenerator.isSupportedNode(it) },
            PluginOptions.copyReflectShortcut
        ) { node ->
            if (!PluginOptions.advancedMode || !KeyboardManager.isShiftDown) {
                val code = ReflectCodeGenerator.generate(node)
                gui.copyToClipboard(code)
                return@addPopupMenuAction
            }

            AdvancedDialog(gui).show()
        }

        KeyboardManager.init()
    }
}