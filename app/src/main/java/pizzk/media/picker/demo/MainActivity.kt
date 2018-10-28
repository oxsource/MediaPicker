package pizzk.media.picker.demo

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import pizzk.media.picker.arch.CropParams
import pizzk.media.picker.arch.MimeType
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.utils.PickPhotoHelper
import pizzk.media.picker.widget.PhotoGroupView

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PickControl.authority(getString(R.string.file_provider))
        //多张选择示例
        val photoGroup: PhotoGroupView = findViewById(R.id.photoGroup)
        val size: Int = resources.getDimensionPixelSize(R.dimen.x75)
        val tvHint: TextView = findViewById(R.id.tvHint)
        val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(size, size)
        val special: PhotoGroupView.Special = PhotoGroupView.Special(this@MainActivity, lp = lp, limit = 4, column = 4)
        photoGroup.setup(special, emptyList(), false) {
            tvHint.text = "(${it.selectCount()}/${special.limit})"
        }
        //单张选择示例
        val ivSingle: ImageView = findViewById(R.id.ivSingle)
        val crop = CropParams(aspectX = 1, aspectY = 1)
        ivSingle.setOnClickListener {
            PickPhotoHelper.show(this@MainActivity, 1, crop, callback = { _, uris ->
                val uri: Uri = uris[0]
                PickControl.imageLoad().load(ivSingle, uri, MimeType.JPEG.mime)
            })
        }
    }
}
