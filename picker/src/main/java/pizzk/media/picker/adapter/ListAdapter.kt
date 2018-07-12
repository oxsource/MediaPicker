package pizzk.media.picker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class ListAdapter<T>(protected val context: Context) : RecyclerView.Adapter<ViewHolder>() {
    private val data: MutableList<T> = ArrayList()
    protected abstract fun getLayoutId(viewType: Int): Int
    //holder,index
    private var tapNormal: (ViewHolder, Int) -> Unit = { _, _ -> }
    //holder,index,what
    protected open var tapChild: (ViewHolder, View, Int, Int) -> Unit = { _, _, _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val lyId: Int = getLayoutId(viewType)
        val view: View = LayoutInflater.from(context).inflate(lyId, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val index: Int = holder.adapterPosition
            if (index in 0..getList().size) {
                tapNormal(holder, index)
            }
        }
        return holder
    }

    fun append(d: List<T>?, clean: Boolean = false) {
        if (clean) data.clear()
        if (null == d || d.isEmpty()) return
        data.addAll(d)
    }

    fun remove(index: Int): Boolean {
        if (index !in 0..data.size) return false
        data.removeAt(index)
        return true
    }

    fun removeAll() {
        data.clear()
    }

    override fun getItemCount(): Int = data.size

    fun getList(): MutableList<T> = data

    fun setTapBlock(tap: (holder: ViewHolder, index: Int) -> Unit) {
        tapNormal = tap
    }

    fun setTapChildBlock(tap: (holder: ViewHolder, view: View, index: Int, what: Int) -> Unit) {
        tapChild = tap
    }

    companion object {
        const val WHAT0: Int = 100
        const val WHAT1: Int = 101
        const val WHAT2: Int = 102
        const val WHAT3: Int = 103
    }
}