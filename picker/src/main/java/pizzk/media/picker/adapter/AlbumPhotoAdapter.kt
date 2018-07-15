package pizzk.media.picker.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem

class AlbumPhotoAdapter(context: Context) : ListAdapter<AlbumItem>(context) {
    private val selectedList: MutableList<AlbumItem> = ArrayList()
    private var selectBlock: (List<AlbumItem>) -> Unit = { _ -> }

    override fun getLayoutId(viewType: Int): Int = R.layout.album_photo_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: AlbumItem = getList()[position]
        holder.itemView.tag = item
        //控件初始化
        val image: ImageView = holder.getView(R.id.image)!!
        val check: ImageView = holder.getView(R.id.check)!!
        val maskView: View = holder.getView(R.id.mask)!!
        //填充视图
        PickControl.imageLoad().load(image, item.getUri(), item.getMime())
        //选择
        updateCheckState(item, check, maskView)
        check.setOnClickListener {
            val selected: Boolean = getSelectList().contains(item)
            if (selected) {
                selectedList.remove(item)
                updateCheckState(item, check, maskView)
                selectBlock(getSelectList())
                return@setOnClickListener
            }
            val limit: Int = PickControl.obtain().limit()
            if (selectedList.size >= limit) {
                val hint: String = context.getString(R.string.pick_media_most_select_limit)
                val content: String = String.format(hint, limit)
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedList.add(item)
            updateCheckState(item, check, maskView)
            selectBlock(getSelectList())
        }
    }

    //更新选中状态
    private fun updateCheckState(item: AlbumItem, view: ImageView, mask: View) {
        val selected: Boolean = getSelectList().contains(item)
        if (selected) {
            mask.visibility = View.VISIBLE
            view.setImageResource(R.drawable.album_check_active)
        } else {
            mask.visibility = View.GONE
            view.setImageResource(R.drawable.album_check_normal)
        }
    }

    fun setSelectBlock(block: (List<AlbumItem>) -> Unit) {
        this.selectBlock = block
    }

    fun getSelectList(): List<AlbumItem> = selectedList

    fun updateSelectList(selects: List<AlbumItem>?) {
        val list: List<AlbumItem> = selects ?: return
        selectedList.clear()
        selectedList.addAll(list)
        notifyDataSetChanged()
        selectBlock(getSelectList())
    }
}