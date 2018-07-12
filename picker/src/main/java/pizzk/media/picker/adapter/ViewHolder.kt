package pizzk.media.picker.adapter

import android.graphics.drawable.Drawable
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

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

    fun setHint(@IdRes id: Int, text: CharSequence?) {
        if (null == text) return
        val view: View = getView(id) ?: return
        when (view) {
            is EditText -> view.hint = text
        }
    }

    fun setImageDrawable(@IdRes id: Int, res: Int) {
        val view: View = getView(id) ?: return
        val drawable: Drawable = ContextCompat.getDrawable(view.context, res) ?: return
        when (view) {
            is ImageView -> view.setImageDrawable(drawable)
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