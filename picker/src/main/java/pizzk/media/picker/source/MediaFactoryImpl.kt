package pizzk.media.picker.source

class MediaFactoryImpl : MediaFactory(capacity = 512) {

    override fun supply(): IMedia = MediaImpl()
}