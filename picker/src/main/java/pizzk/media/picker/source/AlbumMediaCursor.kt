package pizzk.media.picker.source

import android.database.Cursor
import android.database.MergeCursor
import android.text.TextUtils

class AlbumMediaCursor(
    private val sources: List<MediaSource>,
    private val cursors: Array<Cursor>
) : MergeCursor(cursors) {
    private var cursor: Cursor? = null

    fun counts(): Int {
        val cursor = cursor ?: return super.getCount()
        return cursor.count
    }

    fun moveToBucket(id: String?, defaultVal: Int): Int {
        if (id.isNullOrEmpty()) {
            cursor = null
            return 0
        }
        var offset = 0
        cursors.mapIndexed { index, cursor ->
            if (TextUtils.equals(sources[index].mBucketId, id)) {
                this.cursor = cursor
                return offset
            }
            offset += cursor.count
        }
        return defaultVal
    }
}