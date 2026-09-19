package cc.meteormc.xposedkit.jadx.ui

import cc.meteormc.xposedkit.jadx.util.I18n
import jadx.api.plugins.gui.JadxGuiContext
import javax.swing.JDialog

@Suppress("OVERRIDE_DEPRECATION")
class AdvancedDialog(gui: JadxGuiContext) : JDialog(gui.mainFrame) {
    init {
        title = I18n.str("dialog.advanced.title")

        pack()
		setSize(800, 500)
		setLocationRelativeTo(null)
		setDefaultCloseOperation(DISPOSE_ON_CLOSE)
		setModalityType(ModalityType.MODELESS)
    }

    override fun show() {
        @Suppress("DEPRECATION")
        super.show()
    }

    override fun hide() {
        @Suppress("DEPRECATION")
        super.hide()
    }
}