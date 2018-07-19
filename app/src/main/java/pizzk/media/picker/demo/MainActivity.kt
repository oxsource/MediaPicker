package pizzk.media.picker.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.TextView
import pizzk.media.picker.widget.PhotoGroupView

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val photoGroup: PhotoGroupView = findViewById(R.id.photoGroup)
        val size: Int = resources.getDimensionPixelSize(R.dimen.x75)
        val tvHint: TextView = findViewById(R.id.tvHint)
        val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(size, size)
        val special: PhotoGroupView.Special = PhotoGroupView.Special(
                this@MainActivity, lp = lp, limit = 4,
                authority = getString(R.string.file_provider), column = 4
        )
        photoGroup.setup(special) {
            tvHint.text = "(${it.selectCount()}/${special.limit})"
        }
    }
}
