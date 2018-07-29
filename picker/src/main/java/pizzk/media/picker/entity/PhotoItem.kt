package pizzk.media.picker.entity

import android.os.Parcel
import android.os.Parcelable

data class PhotoItem(
        var path: String = "",
        var desc: String? = null,
        var which: Int = -1
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(desc)
        parcel.writeInt(which)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhotoItem> {
        override fun createFromParcel(parcel: Parcel): PhotoItem {
            return PhotoItem(parcel)
        }

        override fun newArray(size: Int): Array<PhotoItem?> {
            return arrayOfNulls(size)
        }
    }
}