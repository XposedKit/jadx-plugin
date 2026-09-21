@file:Suppress("UNCHECKED_CAST")

package cc.meteormc.xposedkit.jadx.ui

import cc.meteormc.xposedkit.jadx.data.MatchMode
import cc.meteormc.xposedkit.jadx.generator.BaseGenerator
import cc.meteormc.xposedkit.jadx.generator.ClassReflectGenerator
import cc.meteormc.xposedkit.jadx.generator.FieldReflectGenerator
import cc.meteormc.xposedkit.jadx.generator.MethodReflectGenerator
import cc.meteormc.xposedkit.jadx.util.I18n
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.FieldNode
import jadx.core.dex.nodes.ICodeNode
import jadx.core.dex.nodes.MethodNode
import jadx.gui.plugins.context.GuiPluginContext
import jadx.gui.ui.codearea.AbstractCodeArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.*

@Suppress("OVERRIDE_DEPRECATION")
class AdvancedDialog(
    private val node: ICodeNode,
    private val gui: GuiPluginContext
) : JDialog(gui.mainFrame) {
    private val mainWindow = gui.commonContext.mainWindow
    private val codeArea = AbstractCodeArea.getDefaultArea(mainWindow)
    private val generator = when (node) {
        is ClassNode -> ClassReflectGenerator
        is MethodNode -> MethodReflectGenerator
        is FieldNode -> FieldReflectGenerator
        else -> throw IllegalArgumentException("Unsupported node type: ${node::class.simpleName}")
    } as BaseGenerator<ICodeNode>
    private var matchMode = generator.getPreferredMode(node)
    private var useLambda = true

    init {
        title = I18n.str("advanced-dialog.title")

        codeArea.rows = 5
        codeArea.isEditable = true
        codeArea.border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
        codeArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_KOTLIN

        val contentPanel = JPanel(BorderLayout(5, 5))
        contentPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        contentPanel.add(buildOptionsPanel(), BorderLayout.PAGE_START)
        contentPanel.add(buildPreviewPanel(), BorderLayout.CENTER)
        contentPanel.add(buildButtonPanel(), BorderLayout.PAGE_END)
        contentPane.add(contentPanel)

        pack()
		setSize(500, 400)
		setLocationRelativeTo(null)
		setDefaultCloseOperation(DISPOSE_ON_CLOSE)
		setModalityType(ModalityType.MODELESS)

        updateCodeArea()
    }

    override fun show() {
        @Suppress("DEPRECATION")
        super.show()
    }

    override fun hide() {
        @Suppress("DEPRECATION")
        super.hide()
    }

    private fun updateCodeArea() {
        codeArea.text = generator.generate(node, matchMode, useLambda)
        codeArea.caretPosition = 0
    }

    private fun buildOptionsPanel(): Component {
        val disabled = generator.getDisabledModes(node)

        val modeButtonGroup = ButtonGroup()
        val modeButtonPanel = JPanel()
        modeButtonPanel.setLayout(BoxLayout(modeButtonPanel, BoxLayout.Y_AXIS))
        fun addMatchModeCheckBox(
            name: String,
            target: MatchMode,
        ) {
            val cb = JCheckBox(name)
            cb.isEnabled = !disabled.contains(target)
            cb.isSelected = matchMode == target
            cb.addItemListener {
                if (it.getStateChange() != ItemEvent.SELECTED) return@addItemListener
                matchMode = target
                updateCodeArea()
            }
            modeButtonGroup.add(cb)
            modeButtonPanel.add(cb)
        }

        addMatchModeCheckBox(I18n.str("advanced-dialog.standard-matching"), MatchMode.STANDARD)
        addMatchModeCheckBox(I18n.str("advanced-dialog.exact-matching"), MatchMode.EXACT)
        addMatchModeCheckBox(I18n.str("advanced-dialog.feature-matching"), MatchMode.FEATURE)

        val useLambdaButton = JCheckBox(I18n.str("advanced-dialog.use-lambda"))
        useLambdaButton.isSelected = useLambda
        useLambdaButton.addActionListener {
            useLambda = useLambdaButton.isSelected
            updateCodeArea()
        }
        useLambdaButton.alignmentY = TOP_ALIGNMENT

        val useLambdaPanel = JPanel(BorderLayout())
        useLambdaPanel.preferredSize = Dimension(
            useLambdaButton.preferredSize.width,
            modeButtonPanel.preferredSize.height
        )
        useLambdaPanel.add(useLambdaButton, BorderLayout.PAGE_START)

        val optionsPanel = JPanel()
        optionsPanel.setLayout(FlowLayout(FlowLayout.LEFT, 10, 0))
        optionsPanel.setBorder(BorderFactory.createTitledBorder(I18n.str("advanced-dialog.options")))
        optionsPanel.add(modeButtonPanel)
        optionsPanel.add(Box.createHorizontalGlue())
        optionsPanel.add(useLambdaPanel)
        return optionsPanel
    }

    private fun buildPreviewPanel(): Component {
        val scrollPane = RTextScrollPane(codeArea)
        scrollPane.lineNumbersEnabled = false
        scrollPane.border = BorderFactory.createEmptyBorder()
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER

        val previewPanel = JPanel(BorderLayout())
        previewPanel.setBorder(BorderFactory.createTitledBorder(I18n.str("advanced-dialog.preview")))
        previewPanel.add(scrollPane, BorderLayout.CENTER)
        return previewPanel
    }

    private fun buildButtonPanel(): Component {
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