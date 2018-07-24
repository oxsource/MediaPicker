package pizzk.media.picker.adapter

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
    private val isAppend: Boolean
    private val lp: ViewGroup.LayoutParams
    private var changeBlock: (PhotoGroupAdapter) -> Unit = { _ -> }

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

    fun setReadOnly(value: Boolean) {
        if (value == readOnly) return
        readOnly = value
        notifyDataSetChanged()
    }

    fun isReadOnly() = readOnly

    fun setChangeBlock(block: (PhotoGroupAdapter) -> Unit) {
        this.changeBlock = block
    }

    //删除指定位置图片
    fun delete(index: Int) {
        if (index !in 0 until itemCount) return
        val el: PhotoItem = getList()[index]
        if (!isAppend) {
            el.path = ""
            notifyItemChanged(index)
            return
        }
        if (1 == getList().size) {
            el.path = ""
            notifyItemChanged(index)
            return
        }
        remove(index)
        if (!TextUtils.isEmpty(getList()[getList().size - 1].path)) {
            getList().add(PhotoItem())
        }
        notifyDataSetChanged()
        changeBlock(this)
    }

    //更新图片
    fun update(list: List<String>?, limit: Int): Boolean {
        if (limit <= 0) return false
        if (null == list || list.isEmpty()) return false
        if (isAppend) {
            getList().clear()
            for (i: Int in 0 until min(limit, list.size)) {
                getList().add(PhotoItem(path = list[i]))
            }
            //判断是否还可选
            if (getList().size < limit) {
                getList().add(PhotoItem())
            }
        } else {
            for (i: Int in 0 until min(getList().size, list.size)) {
                getList()[i].path = list[i]
            }
        }
        notifyDataSetChanged()
        changeBlock(this)
        return true
    }

    fun selects(): List<String> {
        val size: Int = selectCount()
        val list: MutableList<String> = ArrayList(size)
        for (i: Int in 0 until getList().size) {
            val el: String = getList()[i].path
            if (!TextUtils.isEmpty(el)) {
                list.add(el)
            }
        }
        return list
    }

    fun selectCount() = getList().count { !TextUtils.isEmpty(it.path) }

    override fun getLayoutId(viewType: Int): Int = R.layout.pick_photo_list_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.layoutParams = lp
        val el: PhotoItem = getList()[position]
        if (isAppend) {
            holder.setVisibility(R.id.tvHint, View.GONE)
            holder.setText(R.id.tvHint, "")
        } else {
            holder.setVisibility(R.id.tvHint, View.VISIBLE)
            holder.setText(R.id.tvHint, el.desc)
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