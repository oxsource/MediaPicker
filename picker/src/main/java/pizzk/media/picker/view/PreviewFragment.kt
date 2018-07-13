package pizzk.media.picker.view

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pizzk.media.picker.R

class PreviewFragment : BaseFragment() {
    private lateinit var toolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: ViewGroup = inflater.inflate(getLayoutId(), null, false) as ViewGroup
        toolbar = view.findViewById(R.id.toolbar)
        return view
    }

    override fun getLayoutId(): Int = R.layout.fragment_preview

    override fun getToolbar(): Toolbar = toolbar
}