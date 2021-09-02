package pizzk.media.picker.entity

import android.net.Uri
import pizzk.media.picker.source.MediaSource

data class AlbumBucket(
    val id: String,
    val name: String,
    val size: Int,
    val cover: Uri?,
    val mime: String,
    var select: Boolean = false,
) {
    companion object {
        fun of(id: String, name: String, source: MediaSource): AlbumBucket {
            val count = source.count()
            val media = if (count > 0) source[0] else null
            val cover = media?.uri()
            val mime = media?.mimeType() ?: ""
            return AlbumBucket(id, name, count, cover, mime)
        }
    }
}