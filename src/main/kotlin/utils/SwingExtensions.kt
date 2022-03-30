package utils

import java.awt.Color
import java.awt.Component
import java.awt.Container
import javax.swing.JComboBox
import javax.swing.JLabel


fun Array<Component>.setPickFolderJFileChooser() {
    for (i in indices) {
        val c: Component = get(i)
        if (!c.isVisible) {
            return
        }
        // hide file name selection
        if (c::class.java.simpleName.equals("MetalFileChooserUI$3")) {
            c.parent.isVisible = false
        }

        if (c is JComboBox<*>) {
            if (c.selectedItem.toString().contains("AcceptAllFileFilter")) {
                c.setVisible(false)
            }

        } else if (c is JLabel) {
            val text = c.text
            if (text == "Look in:") {
                c.text = ""
            }
            if (text.lowercase() == "Files of type:".lowercase()) {
                c.getParent().isVisible = false
                c.parent.parent.components.forEachIndexed { indexF, component ->
                    if (component is Container) {
                        component.components.forEachIndexed { indexS, component ->
                            if (indexF == 2) {
                                component.isEnabled = false
                            }
                        }
                    }

                }
            }
        }
        if (c is Container) c.components.setPickFolderJFileChooser()
        c.background = Color(240,240,240)
    }
}