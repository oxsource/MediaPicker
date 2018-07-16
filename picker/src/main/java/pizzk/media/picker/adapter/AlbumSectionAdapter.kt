package pizzk.media.picker.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem
import pizzk.media.picker.entity.AlbumSection
import pizzk.media.picker.utils.PickUtils

/**
 * 相册目录适配器
 */
class AlbumSectionAdapter(context: Context) : CommonListAdapter<AlbumSection>(context) {

    init {
        val sections: List<AlbumSection> = PickUtils.loadImages(context)
        sections[0].name = context.getString(R.string.pick_media_all_picture)
        append(sections, true)
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.album_section_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val el: AlbumSection = getList()[position]
        val child: List<AlbumItem> = getAlbumsBySectionIndex(position)
        if (child.isNotEmpty()) {
            val item: AlbumItem = child.first()
            val image: ImageView = holder.getView(R.id.image)!!
            PickControl.imageLoad().load(image, item.getUri(), item.getMime())
        }
        holder.setText(R.id.tvName, el.name)
        holder.setText(R.id.tvNum, "${child.size}")
        holder.setVisibility(R.id.vSelect, if (el.select) View.VISIBLE else View.GONE)
    }

    //仅显示某个section
    fun getAlbumsBySectionIndex(index: Int): List<AlbumItem> {
        if (index < 0 || index >= getList().size) return emptyList()
        return if (0 == index) {
            getList().flatMap { it.content }
        } else {
            getList()[index].content
        }
    }

    //获取选中的Section
    fun getSelectSection(): AlbumSection? = getList().findLast { it.select }
}