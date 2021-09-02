package pizzk.media.picker.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumBucket

/**
 * 相册目录适配器
 */
class AlbumBucketAdapter(context: Context) : CommonListAdapter<AlbumBucket>(context) {
    override fun getLayoutId(viewType: Int): Int = R.layout.album_section_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val el: AlbumBucket = getList()[position]
        holder.setText(R.id.tvName, el.name)
        holder.setText(R.id.tvNum, "${el.size}")
        holder.setVisibility(R.id.vSelect, if (el.select) View.VISIBLE else View.GONE)
        val image: ImageView = holder.getView(R.id.image) ?: return
        val path = el.cover ?: return
        PickControl.imageLoad().load(image, path, el.mime)
    }

    //获取选中的Section
    fun getSelectBucket(): AlbumBucket? = getList().findLast { it.select }
}