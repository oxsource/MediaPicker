package pizzk.media.picker.demo

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import pizzk.media.picker.arch.CropParams
import pizzk.media.picker.arch.MimeType
import pizzk.media.picker.arch.PickControl

class MainActivity : AppCompatActivity() {
    private lateinit var label: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PickControl.authority(getString(R.string.file_provider))
        setContentView(R.layout.activity_main)
        label = findViewById(R.id.label)
        label.text = "未选择照片"
        button = findViewById(R.id.button)
        val cropParams = CropParams(
                aspectX = 1, aspectY = 1,
                outputX = 400, outputY = 400)
        PickControl.obtain().action(PickControl.ACTION_ALBUM)
                .filter { uri, mime ->
                    null !== uri && (mime in MimeType.ofImage().map { it.mime })
                }.limit(1)
                .crop(cropParams)
                .callback(MainActivity@ this::onPickSuccess)
        button.setOnClickListener {
            PickControl.obtain().done(MainActivity@ this)
        }
    }

    private fun onPickSuccess(uris: List<Uri>) {
        if (uris.isEmpty()) return
        label.text = uris[0].toString()
    }
}
