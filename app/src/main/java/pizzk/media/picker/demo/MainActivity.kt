package pizzk.media.picker.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import pizzk.media.picker.view.PickPhotoGroupView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val photoGroup: PickPhotoGroupView = findViewById(R.id.photoGroup)
        val size: Int = resources.getDimensionPixelSize(R.dimen.x75)
        val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(size, size)
        val special: PickPhotoGroupView.Special = PickPhotoGroupView.Special(
                this@MainActivity, lp = lp, limit = 4,
                authority = getString(R.string.file_provider), column = 4
        )
        photoGroup.build(special)
    }
}
