package pizzk.media.picker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.utils.PickUtils

/**
 * 预览选适配器
 */
class PreviewSelectAdapter(context: Context, list: List<String>) : CommonListAdapter<String>(context) {
    private var sameIndex: Int = -1
    private var clickBlock: (String) -> Unit = { _ -> }
    private val marginRight: Int by lazy {
        context.resources.getDimensionPixelOffset(R.dimen.album_preview_select_item_padding)
    }

    init {
        append(list, true)
    }

    override fun getLayoutId(viewType: Int) = R.layout.preview_select_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path: String = getList()[position]
        holder.itemView.setOnClickListener {
            clickBlock(path)
        }
        val photo: ImageView = holder.getView(R.id.iv)!!
        val mime: String = PickUtils.getImageMime(context, path)
        PickControl.imageLoad().load(photo, path, mime)
        val vi: Int = if (position == sameIndex) View.VISIBLE else View.GONE
        holder.setVisibility(R.id.mask, vi)
        val isLast: Boolean = position == itemCount - 1
        val lp: ViewGroup.MarginLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        lp.rightMargin = if (isLast) marginRight else 0
    }

    fun setClickListener(block: (String) -> Unit) {
        this.clickBlock = block
    }

    fun onPreviewChanged(uri: String?, view: RecyclerView): Boolean {
        val lastIndex: Int = sameIndex
        sameIndex = -1
        uri?.let {
            for (i: Int in 0 until getList().size) {
                if (uri == getList()[i]) {
                    sameIndex = i
                    break
                }
            }
        }
        if (lastIndex >= 0) notifyItemChanged(lastIndex)
        if (sameIndex >= 0) {
            view.scrollToPosition(sameIndex)
            notifyItemChanged(sameIndex)
            return true
        }
        return false
    }
}