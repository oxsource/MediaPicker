package pizzk.media.picker.source

import android.database.Cursor
import java.util.*

abstract class MediaFactory(
    private val capacity: Int,
    initialCapacity: Int = 16,
    loadFactor: Float = 0.75f,
    accessOrder: Boolean = true
) : LinkedHashMap<Int, IMedia>(initialCapacity, loadFactor, accessOrder) {
    private val recycles: MutableList<IMedia> = LinkedList()

    open fun loadFromCursor(source: IMediaSource, cursor: Cursor): IMedia {
        val media = if (recycles.isEmpty()) supply() else recycles.removeAt(0)
        media.of(source, cursor)
        return media
    }

    abstract fun supply(): IMedia

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, IMedia>?): Boolean {
        if (size <= capacity) return false
        if (recycles.size < capacity && null != eldest) {
            eldest.value.recycle()
            recycles.add(eldest.value)
        }
        return true
    }

    override fun clear() {
        super.clear()
        recycles.clear()
    }
}