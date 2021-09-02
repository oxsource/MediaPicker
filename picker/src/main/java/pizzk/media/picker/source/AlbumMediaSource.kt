package pizzk.media.picker.source

import android.content.Context
import android.provider.MediaStore
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
    private var bucketId = ""

    init {
        val resolver = context.contentResolver
        buckets.putAll(super.bucketIds())
        for ((bucketId) in buckets) {
            sources.add(MediaSourceImpl(resolver, mBaseUri, isAsc, bucketId, MediaFactoryImpl()))
        }
    }

    override fun bucketIds(): Map<String, String> = buckets

    override fun count(): Int {
        val source = sources.find { it.mBucketId == bucketId } ?: return super.count()
        return source.count()
    }

    override fun close() {
        super.close()
        buckets.clear()
        sources.forEach { it.close() }
        sources.clear()
    }

    override fun get(i: Int): IMedia? {
        val source = sources.find { it.mBucketId == bucketId } ?: return super.get(i)
        return source[i]
    }

    /**
     * 通过id指定当前MediaSource并计算偏移量
     *
     * @param id bucket id
     */
    fun use(id: String?): AlbumMediaSource {
        bucketId = id ?: ""
        return this
    }
}