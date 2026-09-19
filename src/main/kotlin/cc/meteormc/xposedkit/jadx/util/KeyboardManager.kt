package cc.meteormc.xposedkit.jadx.util

import java.awt.KeyboardFocusManager
import java.awt.event.InputEvent

object KeyboardManager {
    private var modifiers: Int = 0
    val isShiftDown
        get() = (modifiers and InputEvent.SHIFT_DOWN_MASK) != 0
    val isControlDown
        get() = (modifiers and InputEvent.CTRL_DOWN_MASK) != 0
    val isMetaDown
        get() = (modifiers and InputEvent.META_DOWN_MASK) != 0
    val isAltDown
        get() = (modifiers and InputEvent.ALT_DOWN_MASK) != 0

    fun init() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher {
            modifiers = it.modifiersEx
            return@addKeyEventDispatcher false
        }
    }
}