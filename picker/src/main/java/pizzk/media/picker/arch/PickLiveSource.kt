package pizzk.media.picker.arch

import android.content.Context
import androidx.lifecycle.MutableLiveData
import pizzk.media.picker.R
import pizzk.media.picker.entity.AlbumBucket
import pizzk.media.picker.source.AlbumMediaSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PickLiveSource : MutableLiveData<PickLiveSource.Data>() {
    class Data(
        val source: AlbumMediaSource,
        val buckets: List<AlbumBucket>
    )

    private val tPool: ExecutorService = Executors.newScheduledThreadPool(1)

    fun load(context: Context) {
        value?.source?.close()
        tPool.execute {
            kotlin.runCatching {
                val source = AlbumMediaSource(context)
                val buckets = source.bucketIds()
                val sections: MutableList<AlbumBucket> = ArrayList(buckets.size + 1)
                val name = context.getString(R.string.pick_media_all_picture)
                source.use(id = null)
                sections.add(AlbumBucket.of(id = "", name = name, source = source))
                buckets.forEach { e ->
                    source.use(e.key)
                    sections.add(AlbumBucket.of(id = e.key, name = e.value, source = source))
                }
                source.use(id = null)
                sections[0].select = true
                postValue(Data(source, sections))
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun source(): AlbumMediaSource? = value?.source

    fun buckets(): List<AlbumBucket> = value?.buckets ?: emptyList()
}