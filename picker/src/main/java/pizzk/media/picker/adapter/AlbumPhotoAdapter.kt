package pizzk.media.picker.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import pizzk.media.picker.R
import pizzk.media.picker.arch.MimeType
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem
import pizzk.media.picker.utils.PickUtils

class AlbumPhotoAdapter(context: Context) : ListAdapter<AlbumItem>(context) {
    private val selectedList: MutableList<AlbumItem> = ArrayList()
    private var selectBlock: (List<AlbumItem>) -> Unit = { _ -> }
    //分页加载相关
    private var loadFlag: Boolean = true
    private var loadPage: Int = 0


    init {
        loadContent()
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.album_photo_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: AlbumItem = getList()[position]
        holder.itemView.tag = item
        //控件初始化
        val image: ImageView = holder.getView(R.id.image)!!
        val check: ImageView = holder.getView(R.id.check)!!
        val maskView: View = holder.getView(R.id.mask)!!
        //填充视图
        Glide.with(context).load(item.getUri()).transition(withCrossFade()).into(image)
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
                val hint: String = context.getString(R.string.hint_most_select_limit)
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

    //分页加载内容
    fun loadContent(page: Int = loadPage) {
        if (0 == page) {
            loadPage = 0
            loadFlag = true
            getList().filter { !selectedList.contains(it) }.forEach { AlbumItem.recycle(it) }
            getList().clear()
        } else if (page < loadPage) {
            return
        }
        if (!loadFlag) return
        PickUtils.loadContent(context, MimeType.JPEG, page) {
            loadFlag = it.count >= PickUtils.ALBUM_PAGE_SIZE
            loadPage = page + 1
            val list: MutableList<AlbumItem> = ArrayList(it.count)
            for (i: Int in 0 until it.count) {
                if (!it.moveToPosition(i)) break
                list.add(AlbumItem.obtain(it))
            }
            if (list.isEmpty()) return@loadContent
            val lastIndex: Int = getList().size
            append(list, false)
            notifyItemRangeInserted(lastIndex, list.size)
        }
    }

    fun setSelectBlock(block: (List<AlbumItem>) -> Unit) {
        this.selectBlock = block
    }

    fun getSelectList(): List<AlbumItem> = selectedList
}