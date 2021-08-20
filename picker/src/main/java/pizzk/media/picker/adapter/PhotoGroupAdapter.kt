package pizzk.media.picker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.PhotoItem
import pizzk.media.picker.utils.PickUtils
import kotlin.math.min

class PhotoGroupAdapter(context: Context, fixedList: List<PhotoItem>?, lp: ViewGroup.LayoutParams)
    : CommonListAdapter<PhotoItem>(context) {
    private var readOnly: Boolean = false
    val isAppend: Boolean
    private var appendText: String = ""
    private val lp: ViewGroup.LayoutParams
    private var changeBlock: (PhotoGroupAdapter, Int) -> Unit = { _, _ -> }

    init {
        if (null == fixedList || fixedList.isEmpty()) {
            isAppend = true
            getList().add(PhotoItem())
        } else {
            isAppend = false
            getList().addAll(fixedList)
        }
        this.lp = lp
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setReadOnly(value: Boolean) {
        if (value == readOnly) return
        readOnly = value
        notifyDataSetChanged()
    }

    fun isReadOnly() = readOnly

    fun setAppendText(text: String) {
        appendText = text
    }

    fun setChangeBlock(block: (PhotoGroupAdapter, Int) -> Unit) {
        this.changeBlock = block
    }

    //删除指定位置图片
    fun delete(index: Int) {
        if (index < 0 || index >= getList().size) return
        val el: PhotoItem = getList()[index]
        if (!isAppend || 1 == getList().size) {
            el.path = ""
            notifyItemChanged(index)
            changeBlock(this, index)
            return
        }
        remove(index)
        if (!TextUtils.isEmpty(getList()[getList().size - 1].path)) {
            getList().add(PhotoItem())
        }
        notifyDataSetChanged()
        changeBlock(this, index)
    }

    //更新图片
    fun update(list: List<String>?, limit: Int, index: Int = -1): Boolean {
        if (limit <= 0) return false
        if (null == list || list.isEmpty()) return false
        if (isAppend) {
            getList().clear()
            for (i: Int in 0 until min(limit, list.size)) {
                getList().add(PhotoItem(path = list[i]))
            }
            //判断是否还可选
            if (!readOnly && getList().size < limit) {
                getList().add(PhotoItem())
            }
            notifyDataSetChanged()
        } else {
            if (index >= 0 && index < getList().size) {
                getList()[index].path = list.first()
                notifyItemChanged(index)
            } else {
                return false
            }
        }
        changeBlock(this, index)
        return true
    }

    fun selectItems(): List<PhotoItem> {
        val size: Int = selectCount()
        val list: MutableList<PhotoItem> = ArrayList(size)
        for (i: Int in 0 until getList().size) {
            val path: String = getList()[i].path
            if (!isAppend || !TextUtils.isEmpty(path)) {
                list.add(getList()[i])
            }
        }
        return list
    }

    fun selectPaths(): List<String> = selectItems().map { it.path }

    fun selectCount() = if (isAppend) getList().count { !TextUtils.isEmpty(it.path) } else getList().size

    override fun getLayoutId(viewType: Int): Int = R.layout.pick_photo_list_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.layoutParams = lp
        val el: PhotoItem = getList()[position]
        if (isAppend) {
            val isLast: Boolean = el.path.isEmpty() && position == getList().size - 1
            holder.setText(R.id.tvHint, if (isLast) appendText else "")
        } else {
            val hintText: String = el.desc ?: ""
            holder.setText(R.id.tvHint, hintText)
        }
        if (el.path.isEmpty()) {
            holder.setVisibility(R.id.imgHint, View.VISIBLE)
            holder.setVisibility(R.id.imgTarget, View.GONE)
            holder.setVisibility(R.id.imgDelete, View.GONE)
        } else {
            holder.setVisibility(R.id.imgHint, View.GONE)
            holder.setVisibility(R.id.imgTarget, View.VISIBLE)
            holder.setVisibility(R.id.imgDelete, if (readOnly) View.GONE else View.VISIBLE)
            holder.setClickListen(R.id.imgDelete) { tapChild(holder, it, position, WHAT0) }
            val iv: ImageView = holder.getView(R.id.imgTarget)!!
            val mime: String = PickUtils.getImageMime(context, el.path)
            PickControl.imageLoad().load(iv, el.path, mime)
        }
    }
}