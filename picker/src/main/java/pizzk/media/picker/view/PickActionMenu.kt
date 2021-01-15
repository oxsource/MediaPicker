package pizzk.media.picker.view

import androidx.appcompat.widget.Toolbar
import android.view.MenuItem

/**
 * 标题菜单按钮
 */
class PickActionMenu(toolbar: Toolbar, block: () -> Unit) {

    private val menu: MenuItem = toolbar.menu.add("")

    init {
        menu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.setOnMenuItemClickListener {
            block()
            return@setOnMenuItemClickListener true
        }
    }

    fun item(): MenuItem = menu

    fun enable(value: Boolean) {
        menu.isEnabled = value
    }
}