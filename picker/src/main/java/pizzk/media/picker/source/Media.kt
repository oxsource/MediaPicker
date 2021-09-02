package pizzk.media.picker.source

import android.database.Cursor
import android.net.Uri

interface IMedia {
    fun container(): IMediaSource?
    fun id(): Long
    fun uri(): Uri?
    fun bucketId(): String
    fun index(): Int
    fun title(): String
    fun dateTaken(): Long
    fun duration(): Long
    fun mimeType(): String
    fun mediaType(): Int
    fun of(source: IMediaSource, cursor: Cursor): IMedia
    fun recycle()
}

abstract class Media : IMedia {
    protected var mContainer: IMediaSource? = null
    protected var mId: Long = 0
    protected var mUri: Uri? = null
    protected var mBucketId: String = ""
    protected var mIndex = 0
    protected var mTitle: String = ""
    protected var mDateTaken: Long = 0
    protected var mDuration: Long = 0
    protected var mMimeType: String = ""
    protected var mMediaType = 0

    override fun equals(other: Any?): Boolean {
        val e = other ?: return false
        val media = (e as? Media) ?: return false
        val uri = media.mUri ?: return false
        if (bucketId() != media.mBucketId) return false
        if (id() != media.mId) return false
        if (uri() != uri) return false
        return true
    }

    override fun hashCode(): Int = mUri?.hashCode() ?: super.hashCode()

    override fun container(): IMediaSource? = mContainer

    override fun id(): Long = mId

    override fun uri(): Uri? = mUri

    override fun bucketId(): String = mBucketId

    override fun index(): Int = mIndex

    override fun title(): String = mTitle

    override fun dateTaken(): Long = mDateTaken

    override fun duration(): Long = mDuration

    override fun mimeType(): String = mMimeType

    override fun mediaType(): Int = mMediaType

    override fun recycle() {
        mContainer = null
        mId = 0
        mUri = null
        mBucketId = ""
        mIndex = 0
        mTitle = ""
        mDateTaken = 0
        mDuration = 0
        mMimeType = ""
        mMediaType = 0
    }
}