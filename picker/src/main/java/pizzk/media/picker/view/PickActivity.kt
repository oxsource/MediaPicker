package pizzk.media.picker.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.utils.PickUtils
import java.io.File

class PickActivity : AppCompatActivity() {
    companion object {
        const val TAG = "PickActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick)
        val picker: PickControl = PickControl.obtain(false)
        when (picker.action()) {
            PickControl.ACTION_ALBUM -> {
                PickUtils.launchAlbum(this@PickActivity)
            }
            PickControl.ACTION_CAMERA -> {
                PickUtils.launchCamera(this@PickActivity)
            }
            PickControl.ACTION_PREVIEW -> {
                PickUtils.launchPreview(this@PickActivity)
            }
            PickControl.ACTION_CROP -> {
                PickUtils.launchCrop(this@PickActivity)
            }
            else -> {
                Log.d(TAG, "PickControl not support action: ${picker.action()}")
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!PickUtils.onRequestPermissionResult(this@PickActivity, permissions, grantResults)) {
            finish()
            return
        }
        when (requestCode) {
            PickUtils.REQUEST_CODE_CAMERA -> {
                PickUtils.launchCamera(this@PickActivity)
            }
            PickUtils.REQUEST_CODE_ALBUM -> {
                PickUtils.launchAlbum(this@PickActivity)
            }
            PickUtils.REQUEST_CODE_CROP -> {
                PickUtils.launchCrop(this@PickActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_CANCELED == resultCode) {
            Log.d(TAG, getString(R.string.pick_media_user_cancel))
            finish()
            return
        }
        if (Activity.RESULT_OK != resultCode) {
            finish()
            return
        }
        val picker: PickControl = PickControl.obtain(false)
        when (requestCode) {
            PickUtils.REQUEST_CODE_CAMERA -> {
                val file: File? = PickControl.obtain(false).cameraFile()
                if (null != file) {
                    val uri: Uri = PickUtils.saveToAlbum(baseContext, file)
                    if (null != picker.crop() && picker.limit() == 1) {
                        picker.crop()!!.uri = uri
                        PickUtils.launchCrop(this@PickActivity)
                        return
                    } else {
                        picker.callbacks().invoke(picker.action(), listOf(uri))
                    }
                }
                finish()
            }
            PickUtils.REQUEST_CODE_ALBUM -> {
                val uris: List<Uri> = PickUtils.obtainResultUris(data)
                if (null != picker.crop() && picker.limit() == 1 && uris.isNotEmpty()) {
                    picker.crop()!!.uri = uris[0]
                    PickUtils.launchCrop(this@PickActivity)
                    return
                } else {
                    picker.callbacks().invoke(picker.action(), uris)
                }
                finish()
            }
            PickUtils.REQUEST_CODE_PREVIEW -> {
                finish()
            }
            PickUtils.REQUEST_CODE_CROP -> {
                val file: File? = PickControl.obtain(false).cropFile()
                if (null != file) {
                    val uri: Uri = PickUtils.saveToAlbum(baseContext, file)
                    PickControl.obtain(false).callbacks().invoke(picker.action(), listOf(uri))
                }
                finish()
            }
        }
    }
}