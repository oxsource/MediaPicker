package pizzk.media.picker.entity

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import pizzk.media.picker.arch.MimeType


class AlbumItem private constructor() {
    companion object {
        private val cache: MutableList<AlbumItem> = ArrayList()

        fun obtain(cursor: Cursor): AlbumItem {
            if (cache.isEmpty()) {
                val item = AlbumItem()
                item.cursor(cursor)
                return item
            }
            val index = 0
            val item: AlbumItem = cache[index]
            item.cursor(cursor)
            cache.removeAt(index)
            return item
        }

        fun recycle(item: AlbumItem) {
            item.clean()
            cache.add(item)
        }
    }

    private var id: Long = -1
    private var mime: String = ""
    private var uri: Uri? = null
    private var bucket: String = ""

    private fun cursor(cursor: Cursor?) {
        val c: Cursor = cursor ?: return
        id = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID))
        mime = c.getString(c.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
        val contentUri: Uri = MimeType.getContentUri(mime)
        uri = ContentUris.withAppendedId(contentUri, id)
        bucket = c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
    }

    private fun clean() {
        id = -1
        uri = null
        mime = ""
        bucket = ""
    }

    fun getUri(): Uri? = uri

    fun getBucket(): String = bucket

    fun getMime(): String = mime

    override fun equals(other: Any?): Boolean {
        if (other !is AlbumItem) return false
        val obj: AlbumItem = other
        return obj.id == id && obj.uri == uri
    }

    override fun hashCode(): Int {
        var value = 1
        val rate = 31
        value = rate * value + id.hashCode()
        uri?.let { value = rate * value + it.hashCode() }
        return value
    }
}