package pizzk.media.picker.adapter

import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

@Suppress("UNCHECKED_CAST")
class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val views: SparseArray<View> = SparseArray()

    fun <T : View> getView(@IdRes id: Int): T? {
        var view: View? = views.get(id)
        if (null == view) {
            view = itemView.findViewById<View>(id)?.apply { views.put(id, this) }
        }
        return view as? T
    }

    fun setText(@IdRes id: Int, text: CharSequence?) {
        if (null == text) return
        val view: View = getView(id) ?: return
        when (view) {
            is TextView -> view.text = text
            is EditText -> view.setText(text)
            is Button -> view.text = text
        }
    }

    fun setClickListen(@IdRes id: Int, block: (View) -> Unit) {
        val view: View = getView(id) ?: return
        view.setOnClickListener(block)
    }

    fun setVisibility(@IdRes id: Int, v: Int) {
        val view: View = getView(id) ?: return
        view.visibility = v
    }
}