package com.devmcry.dragger.new

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.devmcry.dragger.R

/**
 *  @author : DevMcryYu
 *  @date : 1/22/21 12:13 PM
 *  @description :
 */
class DecorContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseEditViewContainer(context, attrs, defStyleAttr) {

    private val drawable by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_launcher_foreground,
            context.theme
        )
    }

    override fun onSelected(view: BaseEditView) {
        (view.contentView as ImageView).setImageDrawable(drawable)
    }

    override fun onUnSelected(view: BaseEditView) {
        (view.contentView as ImageView).setImageDrawable(null)
    }

    override fun onAction(view: BaseEditView, type: EditType) {
        Toast.makeText(context, "$type click", Toast.LENGTH_SHORT).show()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view = DecorEditView(context).apply {
            val content = ImageView(context)
            content.layoutParams = LayoutParams(300, 600)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_700, context.theme)
            contentView = content
            angle = 0f
        }
        addView(view)
        val view1 = DecorEditView(context).apply {
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