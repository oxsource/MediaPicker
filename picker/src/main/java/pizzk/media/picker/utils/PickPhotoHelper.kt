package pizzk.media.picker.utils

import android.app.Activity
import android.net.Uri
import pizzk.media.picker.R
import pizzk.media.picker.arch.CropParams
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.view.PickChoseActivity

/**
 * 默认图片拾取类
 */
object PickPhotoHelper {

    fun show(activity: Activity, limit: Int = 1, cropParams: CropParams? = null,
             callback: (action: Int, uris: List<Uri>) -> Unit) {
        val choiceList: List<String> = listOf(
                activity.getString(R.string.pick_chose_camera),
                activity.getString(R.string.pick_chose_album)
        )
        PickChoseActivity.show(activity, choiceList) shown@{ key ->
            val action: Int = when (key) {
                choiceList[0] -> {
                    PickControl.ACTION_CAMERA
                }
                choiceList[1] -> {
                    PickControl.ACTION_ALBUM
                }
                else -> -1
            }
            if (action < 0) return@shown
            PickControl.obtain(true)
                    .action(action)
                    .limit(limit)
                    .callback(callback)
                    .crop(cropParams)
                    .done(activity)
        }
    }
}