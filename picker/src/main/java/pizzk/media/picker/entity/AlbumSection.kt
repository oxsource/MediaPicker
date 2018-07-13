package pizzk.media.picker.entity

data class AlbumSection(var name: String = "",
                        var content: List<AlbumItem> = emptyList(),
                        var select: Boolean = true)