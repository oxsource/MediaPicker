package pizzk.media.picker.view

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pizzk.media.picker.R

/**
 * 基础Fragment
 */
abstract class BaseFragment : Fragment() {
    private lateinit var toolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: ViewGroup = inflater.inflate(R.layout.fragment_base, null, false) as ViewGroup
        toolbar = view.findViewById(R.id.toolbar)
        val body: View = inflater.inflate(getLayoutId(), null, false)
        view.addView(body)
        return view
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    open fun getToolbar(): Toolbar = toolbar

    fun finish() {
        activity?.onBackPressed()
    }
}