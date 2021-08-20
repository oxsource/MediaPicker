package pizzk.media.picker.source

import android.database.Cursor

class MediaImpl : Media() {
    override fun of(source: IMediaSource, cursor: Cursor): IMedia {
        mContainer = source
        mId = cursor.getLong(MediaSourceImpl.INDEX_ID)
        mUri = source.uri(mId)
        mBucketId = cursor.getString(MediaSourceImpl.INDEX_BUCKET_ID)
        mIndex = cursor.position
        mTitle = cursor.getString(MediaSourceImpl.INDEX_DISPLAY_NAME)
        mDateTaken = cursor.getLong(MediaSourceImpl.INDEX_DATE_ADDED)
        mDuration = cursor.getLong(MediaSourceImpl.INDEX_DURATION) / 1000
        mMimeType = cursor.getString(MediaSourceImpl.INDEX_MIME_TYPE)
        mMediaType = cursor.getInt(MediaSourceImpl.INDEX_MEDIA_TYPE)
        return this
    }
}