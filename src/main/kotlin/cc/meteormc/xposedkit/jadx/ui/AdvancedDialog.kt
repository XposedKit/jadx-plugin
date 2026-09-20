package cc.meteormc.xposedkit.jadx.ui

import cc.meteormc.xposedkit.jadx.util.I18n
import jadx.gui.plugins.context.GuiPluginContext
import jadx.gui.ui.codearea.AbstractCodeArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

@Suppress("OVERRIDE_DEPRECATION")
class AdvancedDialog(private var gui: GuiPluginContext) : JDialog(gui.mainFrame) {
    private val mainWindow = gui.commonContext.mainWindow
    private val codeArea = AbstractCodeArea.getDefaultArea(mainWindow)

    init {
        title = I18n.str("advanced-dialog.title")

        codeArea.rows = 5
        codeArea.isEditable = true
        codeArea.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        codeArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_KOTLIN

        val contentPanel = JPanel(BorderLayout(5, 5))
        contentPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        contentPanel.add(initPreviewPanel(), BorderLayout.CENTER)
        contentPanel.add(initButtonPanel(), BorderLayout.PAGE_END)
        contentPane.add(contentPanel)

        pack()
		setSize(500, 600)
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

    private fun initPreviewPanel(): Component {
        val scrollPane = RTextScrollPane(codeArea)
        scrollPane.lineNumbersEnabled = false
        scrollPane.border = BorderFactory.createEmptyBorder()
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER

        val previewPanel = JPanel(BorderLayout())
        previewPanel.setBorder(BorderFactory.createTitledBorder(I18n.str("advanced-dialog.preview")))
        previewPanel.add(scrollPane, BorderLayout.CENTER)
        return previewPanel
    }

    private fun initButtonPanel(): Component {
        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
        buttonPanel.add(Box.createHorizontalGlue())

        val copyButton = JButton(I18n.str("advanced-dialog.copy"))
        copyButton.addActionListener {
            gui.copyToClipboard(codeArea.text)
            dispose()
        }
        rootPane.setDefaultButton(copyButton)

        val cancelButton = JButton(I18n.str("advanced-dialog.cancel"))
        cancelButton.addActionListener { dispose() }

        buttonPanel.add(copyButton)
        buttonPanel.add(Box.createRigidArea(Dimension(10, 0)))
        buttonPanel.add(cancelButton)

        return buttonPanel
    }
}