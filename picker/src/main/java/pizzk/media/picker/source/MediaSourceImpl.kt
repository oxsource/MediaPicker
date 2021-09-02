package pizzk.media.picker.source

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set
import kotlin.collections.toTypedArray

open class MediaSourceImpl(
    resolver: ContentResolver?,
    uri: Uri,
    asc: Boolean,
    bucketId: String,
    factory: MediaFactory
) : MediaSource(resolver, uri, asc, bucketId, factory) {
    private val whereArgs: Array<String>

    override fun bucketIds(): Map<String, String> {
        val buckets: MutableMap<String, String> = HashMap()
        if (!TextUtils.isEmpty(mBucketId)) return buckets
        val uri = mBaseUri.buildUpon().appendQueryParameter("distinct", "true").build()
        val projects = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID
        )
        val fills: (Cursor) -> Unit = { cursor ->
            while (cursor.moveToNext()) {
                buckets[cursor.getString(1)] = cursor.getString(0)
            }
        }
        kotlin.runCatching { query(uri, projects).use(fills) }
        return buckets
    }

    private fun query(
        uri: Uri,
        projects: Array<String>,
        orderBy: String? = null
    ): Cursor {
        val where = whereClause()
        val whereArgs = whereClauseArgs()
        return MediaStore.Images.Media.query(mResolver, uri, projects, where, whereArgs, orderBy)
    }

    protected open fun whereClause(): String {
        return if (TextUtils.isEmpty(mBucketId)) WHERE_CLAUSE else WHERE_CLAUSE_WITH_BUCKET_ID
    }

    protected open fun whereClauseArgs(): Array<String> = whereArgs

    override fun createCursor(): Cursor {
        return query(mBaseUri, PROJECTION, orderBy = sortOrder())
    }

    override fun getMediaId(cursor: Cursor?): Long = cursor?.getLong(INDEX_ID) ?: -1

    override fun sortOrder(): String {
        val ascending = if (isAsc) " ASC" else " DESC"
        val dateExpr = MediaStore.MediaColumns.DATE_ADDED
        return "$dateExpr$ascending"
    }

    init {
        val args: MutableList<String> = ArrayList(3)
        args.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
        args.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
        if (mBucketId.isNotEmpty()) args.add(mBucketId)
        whereArgs = args.toTypedArray()
    }

    companion object {
        private const val WHERE_CLAUSE =
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE}  in (?, ?)) AND ${MediaStore.MediaColumns.SIZE} > 0"
        private const val WHERE_CLAUSE_WITH_BUCKET_ID =
            "$WHERE_CLAUSE AND ${MediaStore.Images.Media.BUCKET_ID} = ?"

        val PROJECTION = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        const val INDEX_ID = 0
        const val INDEX_BUCKET_ID = 1
        const val INDEX_DISPLAY_NAME = 2
        const val INDEX_DATE_ADDED = 3
        const val INDEX_DURATION = 4
        const val INDEX_MIME_TYPE = 5
        const val INDEX_MEDIA_TYPE = 6
    }
}