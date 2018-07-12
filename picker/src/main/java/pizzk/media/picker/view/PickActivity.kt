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
        const val REQUEST_CODE_CAMERA: Int = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)
        val bundle: Bundle = intent.extras
        val picker: PickControl = PickControl.obtain()
        when (picker.action()) {
            PickControl.ACTION_ALBUM -> {
                PickUtils.launchAlbum(PickActivity@ this)
            }
            PickControl.ACTION_CAMERA -> {
                PickUtils.launchCamera(PickActivity@ this, PickActivity.REQUEST_CODE_CAMERA)
            }
            PickControl.ACTION_PREVIEW -> {
                PickUtils.showFragment(PickActivity@ this, PreviewFragment(), bundle)
            }
            PickControl.ACTION_CROP -> {
                PickUtils.showFragment(PickActivity@ this, CropFragment(), bundle)
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
                PickUtils.launchCamera(PickActivity@ this, PickActivity.REQUEST_CODE_CAMERA)
            }
            PickControl.ACTION_ALBUM -> {
                PickUtils.launchAlbum(PickActivity@ this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_CANCELED == resultCode) showToast(getString(R.string.user_cancel))
        if (Activity.RESULT_OK != resultCode) {
            finish()
            return
        }
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                val uri: Uri = PickControl.obtain().cameraUri() ?: return
                PickControl.obtain().callbacks().invoke(listOf(uri))
                finish()
            }
        }
    }


    private fun showToast(text: String) {
        Toast.makeText(PickActivity@ this, text, Toast.LENGTH_SHORT).show()
    }
}