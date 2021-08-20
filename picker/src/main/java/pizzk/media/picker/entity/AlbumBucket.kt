package pizzk.media.picker.entity

import pizzk.media.picker.source.IMedia
import pizzk.media.picker.source.MediaSource

data class AlbumBucket(
    val id: String,
    val name: String,
    val size: Int,
    val cover: IMedia?,
    var select: Boolean = false,
) {
    companion object {
        fun of(id: String, name: String, source: MediaSource): AlbumBucket {
            val count = source.count()
            val cover = source[0]
            return AlbumBucket(id, name, count, cover)
        }
    }
}