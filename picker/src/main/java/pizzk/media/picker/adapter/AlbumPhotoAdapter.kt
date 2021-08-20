package pizzk.media.picker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.source.AlbumMediaSource
import pizzk.media.picker.source.IMedia

class AlbumPhotoAdapter(context: Context) : CommonListAdapter<IMedia>(context) {
    private val selectedList: MutableList<IMedia> = ArrayList()
    private var selectBlock: (List<IMedia>) -> Unit = { _ -> }
    private var selectLimit: Int = 0
    private var source: AlbumMediaSource? = null

    override fun getLayoutId(viewType: Int): Int = R.layout.album_photo_item

    override fun getItemCount(): Int = source?.count() ?: -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = source ?: return
        val item: IMedia = source[position] ?: return
        holder.itemView.tag = item
        //控件初始化
        val image: ImageView = holder.getView(R.id.image)!!
        val check: ImageView = holder.getView(R.id.check)!!
        val maskView: View = holder.getView(R.id.mask)!!
        //填充视图
        PickControl.imageLoad().load(image, item.uri(), item.mimeType())
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
            if (selectedList.size >= selectLimit) {
                val hint: String = context.getString(R.string.pick_media_most_select_limit)
                val content: String = String.format(hint, selectLimit)
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedList.add(item)
            updateCheckState(item, check, maskView)
            selectBlock(getSelectList())
        }
    }

    override fun getItemId(position: Int): Long {
        val source = source ?: return -1
        return source[position]?.id() ?: -1
    }

    fun setSelectLimit(value: Int) {
        if (value < 0 || value == selectLimit) return
        selectLimit = value
    }

    fun getSelectLimit() = selectLimit

    //更新选中状态
    private fun updateCheckState(item: IMedia, view: ImageView, mask: View) {
        val selected: Boolean = getSelectList().contains(item)
        if (selected) {
            mask.visibility = View.VISIBLE
            view.setImageResource(R.drawable.album_check_active)
        } else {
            mask.visibility = View.GONE
            view.setImageResource(R.drawable.album_check_normal)
        }
    }

    fun setSelectBlock(block: (List<IMedia>) -> Unit) {
        this.selectBlock = block
    }

    fun getSelectList(): List<IMedia> = selectedList

    @SuppressLint("NotifyDataSetChanged")
    fun updateSelectList(selects: List<IMedia>?) {
        val list: List<IMedia> = selects ?: return
        selectedList.clear()
        selectedList.addAll(list)
        notifyDataSetChanged()
        selectBlock(getSelectList())
    }

    fun getMedias(values: List<Uri>) = values.mapNotNull { source?.of(it) }

    fun source(value: AlbumMediaSource?) {
        if (null == value) source?.close()
        source = value
    }

    fun bucket(id: String?) {
        source?.use(id)
    }
}