package pizzk.media.picker.source

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

class AlbumMediaSource(context: Context) : MediaSourceImpl(
    context.contentResolver,
    MediaStore.Files.getContentUri("external"),
    asc = false,
    bucketId = "",
    factory = MediaFactoryImpl()
) {
    private val buckets: MutableMap<String, String> = mutableMapOf()
    private val sources: MutableList<MediaSourceImpl> = ArrayList<MediaSourceImpl>()
    private var offset = 0

    init {
        val resolver = context.contentResolver
        buckets.putAll(super.bucketIds())
        for ((bucketId) in buckets) {
            sources.add(MediaSourceImpl(resolver, mBaseUri, true, bucketId, MediaFactoryImpl()))
        }
    }

    override fun bucketIds(): Map<String, String> = buckets

    override fun count(): Int {
        val obj: Cursor = getCursor() as? AlbumMediaCursor ?: return super.count()
        val cursor: AlbumMediaCursor = obj as AlbumMediaCursor
        return cursor.counts()
    }

    override fun createCursor(): Cursor {
        val cursors = sources.map { it.createCursor() }.toTypedArray()
        return AlbumMediaCursor(sources, cursors)
    }

    override fun close() {
        super.close()
        buckets.clear()
        sources.forEach { it.close() }
        sources.clear()
    }

    override fun get(i: Int): IMedia? {
        return super.get(i + offset)
    }

    /**
     * 通过id指定当前MediaSource并计算偏移量
     *
     * @param id bucket id
     */
    fun use(id: String?): AlbumMediaSource {
        val obj: Cursor = getCursor() as? AlbumMediaCursor ?: return this
        val cursor: AlbumMediaCursor = obj as AlbumMediaCursor
        offset = cursor.moveToBucket(id, offset)
        return this
    }
}