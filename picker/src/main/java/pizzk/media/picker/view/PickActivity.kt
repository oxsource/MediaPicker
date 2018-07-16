package pizzk.media.picker.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.utils.PickUtils

class PickActivity : AppCompatActivity() {
    companion object {
        const val TAG = "PickActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick)
        val picker: PickControl = PickControl.obtain()
        when (picker.action()) {
            PickControl.ACTION_ALBUM -> {
                PickUtils.launchAlbum(PickActivity@ this)
            }
            PickControl.ACTION_CAMERA -> {
                PickUtils.launchCamera(PickActivity@ this)
            }
            PickControl.ACTION_PREVIEW -> {
                val uris: List<Uri> = picker.previews()
                val previewIndex: Int = picker.previewsIndex()
                val showSelect = false
                PreviewActivity.show(this@PickActivity, uris, uris, previewIndex, showSelect)
            }
            PickControl.ACTION_CROP -> {
            }
            else -> {
                Log.d(TAG, "PickControl not support action: ${picker.action()}")
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (PickControl.obtain().action()) {
            PickControl.ACTION_CAMERA -> {
                PickUtils.launchCamera(this@PickActivity)
            }
            PickControl.ACTION_ALBUM -> {
                PickUtils.launchAlbum(this@PickActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_CANCELED == resultCode) showToast(getString(R.string.pick_media_user_cancel))
        if (Activity.RESULT_OK != resultCode) {
            finish()
            return
        }
        when (requestCode) {
            PickUtils.REQUEST_CODE_CAMERA -> {
                val uri: Uri = PickControl.obtain().cameraUri() ?: return
                PickControl.obtain().callbacks().invoke(listOf(uri))
                finish()
            }
            PickUtils.REQUEST_CODE_ALBUM -> {
                val finished: Boolean = PickUtils.isResultFinish(data)
                if (finished) {
                    val uris: List<Uri> = PickUtils.obtainResultUris(data)
                    PickControl.obtain().callbacks().invoke(uris)
                }
                finish()
            }
            PickUtils.REQUEST_CODE_PREVIEW -> {
                finish()
            }
        }
    }


    private fun showToast(text: String) {
        Toast.makeText(PickActivity@ this, text, Toast.LENGTH_SHORT).show()
    }
}