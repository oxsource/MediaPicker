package pizzk.media.picker.entity

import android.net.Uri

data class AlbumSection(var name: String = "",
                   var num: Int = 0,
                   var uri: Uri? = null,
                   var select: Boolean = false)