package pizzk.media.picker.source

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import pizzk.media.picker.utils.PickUtils

class PathMediaSource(context: Context, private val paths: List<String>) : MediaSource(
    context.contentResolver,
    MediaStore.Files.getContentUri("external"),
    isAsc = false,
    mBucketId = "",
    MediaFactoryImpl()
) {
    private val media = MediaImpl()

    override fun createCursor(): Cursor {
        val cursor = MatrixCursor(MediaSourceImpl.PROJECTION)
        paths.mapNotNull(PickUtils::path2Uri).forEach { uri ->
            val e: IMedia? = query(uri)
            val id = e?.id() ?: ContentUris.parseId(uri)
            val bucketId = e?.bucketId() ?: mBucketId
            val name = e?.title() ?: ""
            val date = e?.dateTaken() ?: 0
            val duration = e?.duration() ?: 0
            val mineType = e?.mimeType() ?: ""
            val mediaType = e?.mediaType() ?: -1
            cursor.addRow(arrayOf(id, bucketId, name, date, duration, mineType, mediaType))
        }
        return cursor
    }

    private fun query(uri: Uri): IMedia? {
        val resolver = mResolver ?: return null
        return try {
            val cursor = resolver.query(uri, MediaSourceImpl.PROJECTION, null, null, null)
            cursor.use {
                if (!it.moveToFirst()) return null
                return media.of(this, it)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getMediaId(cursor: Cursor?): Long {
        return cursor?.getLong(MediaSourceImpl.INDEX_ID) ?: -1
    }
}