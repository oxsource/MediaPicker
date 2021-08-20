package pizzk.media.picker.utils

import kotlin.math.max
import kotlin.math.min

object TimeUtils {

    fun duration(seconds: Long): String {
        val ss = max(0, seconds)
        val hour = ss / 3600
        val hs = hour * 3600
        val minutes = (ss - hs) / 60
        val ms = minutes * 60
        return arrayOf(min(99, hour), minutes, ss - hs - ms)
            .joinToString(separator = ":") { "$it".padStart(2, '0') }
    }
}