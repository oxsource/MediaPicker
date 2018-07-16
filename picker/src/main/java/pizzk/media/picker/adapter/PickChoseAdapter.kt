package pizzk.media.picker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import pizzk.media.picker.R

class PickChoseAdapter(context: Context) : CommonListAdapter<String>(context) {

    override fun getLayoutId(viewType: Int): Int = R.layout.pick_chose_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp: ViewGroup.MarginLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (0 == position) {
            lp.topMargin = context.resources.getDimensionPixelOffset(R.dimen.pick_chose_bkg_radius)
            lp.bottomMargin = 0
            holder.setVisibility(R.id.vLine, View.VISIBLE)
        } else if (itemCount - 1 == position) {
            lp.topMargin = 0
            lp.bottomMargin = context.resources.getDimensionPixelOffset(R.dimen.pick_chose_bkg_radius)
            holder.setVisibility(R.id.vLine, View.GONE)
        } else {
            lp.topMargin = 0
            lp.bottomMargin = 0
            holder.setVisibility(R.id.vLine, View.VISIBLE)
        }
        val el: String = getList()[position]
        holder.setText(R.id.tvItem, el)
    }
}