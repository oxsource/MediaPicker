package pizzk.media.picker.source

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.util.regex.Pattern

interface IMediaSource {
    fun bucketIds(): Map<String, String>

    fun uri(id: Long): Uri

    fun count(): Int

    operator fun get(i: Int): IMedia?

    fun of(uri: Uri?): IMedia?

    fun close()
}

abstract class MediaSource(
    protected var mResolver: ContentResolver?,
    protected val mBaseUri: Uri,
    protected val isAsc: Boolean,
    val mBucketId: String,
    protected val mFactory: MediaFactory
) : IMediaSource {
    private var mCursor: Cursor? = null

    override fun bucketIds(): Map<String, String> = emptyMap()

    override fun uri(id: Long): Uri {
        return try {
            // does our uri already have an id (single media query)?
            // if so just return it
            val existingId = ContentUris.parseId(mBaseUri)
            if (existingId != id) Log.e(TAG, "id mismatch")
            mBaseUri
        } catch (ex: NumberFormatException) {
            // otherwise tack on the id
            ContentUris.withAppendedId(mBaseUri, id)
        }
    }

    override fun count(): Int {
        val cursor = getCursor() ?: return 0
        synchronized(this) { return cursor.count }
    }

    protected open fun getCursor(): Cursor? {
        synchronized(this) {
            if (null != mCursor) return mCursor
            if (null == mResolver) return null
            mCursor = createCursor()
            return mCursor
        }
    }

    override fun get(i: Int): IMedia? {
        val media: IMedia? = mFactory[i]
        if (null != media) return media
        val cursor = getCursor() ?: return null
        return synchronized(this) {
            if (!cursor.moveToPosition(i)) return@synchronized null
            val value = mFactory.loadFromCursor(this, cursor)
            mFactory[i] = value
            return@synchronized value
        }
    }

    abstract fun createCursor(): Cursor

    abstract fun getMediaId(cursor: Cursor?): Long

    protected open fun invalidateCursor() {
        val cursor = mCursor ?: return
        if (!cursor.isClosed) kotlin.runCatching { cursor.close() }
        mCursor = null
    }

    protected open fun invalidateCache() {
        mFactory.clear()
    }

    private fun isChild(uri: Uri): Boolean {
        // Sometimes, the URI of an media contains a query string with key
        // "bucketId" inorder to restore the media list. However, the query
        // string is not part of the mBaseUri. So, we check only other parts
        // of the two Uri to see if they are the same.
        val base = mBaseUri
        return (base.scheme == uri.scheme
                && base.host == uri.host
                && base.authority == uri.authority
                && base.path == getPathWithoutId(uri))
    }

    override fun of(uri: Uri?): IMedia? {
        if (null == uri || !isChild(uri)) return null
        val matchId: Long = try {
            ContentUris.parseId(uri)
        } catch (ex: NumberFormatException) {
            Log.i(TAG, "fail to get id in: $uri", ex)
            return null
        }
        val cursor = getCursor() ?: return null
        synchronized(this) {
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                if (getMediaId(cursor) != matchId) continue
                val position = cursor.position
                val media: IMedia? = mFactory[position]
                if (null != media) return media
                val value = mFactory.loadFromCursor(this, cursor)
                mFactory[position] = value
                return value
            }
            return null
        }
    }

    // This provides a default sorting order string for subclasses.
    // The list is first sorted by date, then by id. The order can be ascending
    // or descending, depending on the mSort variable.
    // The date is obtained from the "datetaken" column. But if it is null,
    // the "date_modified" column is used instead.
    protected open fun sortOrder(): String {
        val ascending = if (isAsc) " ASC" else " DESC"
        // Use DATE_TAKEN if it's non-null, otherwise use DATE_MODIFIED.
        // DATE_TAKEN is in milliseconds, but DATE_MODIFIED is in seconds.
        val dateExpr = "case ifnull(datetaken,0)" +
                " when 0 then date_modified*1000" +
                " else datetaken" +
                " end"
        // Add id to the end so that we don't ever get random sorting
        // which could happen, I suppose, if the date values are the same.
        return "$dateExpr$ascending, _id$ascending"
    }

    override fun close() {
        invalidateCursor()
        invalidateCache()
        mResolver = null
    }

    companion object {
        const val TAG = "MediaSource"
        private val sPathWithId = Pattern.compile("(.*)/\\d+")

        private fun getPathWithoutId(uri: Uri): String {
            val path = uri.path
            val matcher = sPathWithId.matcher(path)
            return if (matcher.matches()) matcher.group(1) else path
        }
    }
}