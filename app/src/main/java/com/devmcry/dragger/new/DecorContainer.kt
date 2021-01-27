package com.devmcry.dragger.new

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.devmcry.dragger.R

/**
 *  @author : DevMcryYu
 *  @date : 1/22/21 12:13 PM
 *  @description :
 */
class DecorViewGroupNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseEditViewContainer(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view = DecorEditView(context).apply {
            id = 1
            val content = ImageView(context)
            content.layoutParams = LayoutParams(500, 600)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            angle = 0f
        }
        addView(view)
        val view1 = DecorEditView(context).apply {
            id = 2
            val content = ImageView(context)
            content.layoutParams = LayoutParams(400, 400)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            angle = 0f
        }
        addView(view1)
    }

}

fun Any.log(log: String) {
    Log.d("===", log)
}