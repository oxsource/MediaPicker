package pizzk.media.picker.adapter

import android.content.Context
import android.provider.MediaStore
import pizzk.media.picker.R
import pizzk.media.picker.arch.MimeType
import pizzk.media.picker.entity.AlbumSection
import pizzk.media.picker.utils.PickUtils

/**
 * 相册目录适配器
 */
class AlbumSectionAdapter(context: Context) : ListAdapter<AlbumSection>(context) {

    init {
        PickUtils.loadImageSection(context, MimeType.JPEG) {
            while (it.moveToNext()) {
                val name: String = it.getString(it.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
                getList().add(AlbumSection(name))
            }
            notifyDataSetChanged()
        }
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.album_section_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val el: AlbumSection = getList()[position]
        holder.setText(R.id.tvName, el.name)
    }
}