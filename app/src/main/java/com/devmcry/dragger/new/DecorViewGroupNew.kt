package com.devmcry.dragger.new

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
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
) : FrameLayout(context, attrs, defStyleAttr) {
    private var editingViewNew: EffectEditViewNew? = null
    private val drawable by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_launcher_foreground,
            context.theme
        )
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view = EffectEditViewNew(context).apply {
            id = 1
            val content = ImageView(context)
            content.layoutParams = LayoutParams(500,600)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            angle = 76f
        }
        addView(view)
        val view1 = EffectEditViewNew(context).apply {
            id = 2
            val content = ImageView(context)
            content.layoutParams = LayoutParams(400,300)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            angle = 23f
        }
        addView(view1)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        children.iterator().forEach {
            it.layout(it.left, it.top, it.left + it.measuredWidth, it.top + it.measuredHeight)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> {
                editingViewNew = null
            }
        }
//        Log.e("===", "${this.javaClass.name} onTouchEvent ${event.x} ${event.y}")
        if (editingViewNew != null) {
            editingViewNew?.dispatchTouchEvent(event)
        }
        return true
    }

    private var selectViewNew: EffectEditViewNew? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (editingViewNew == null) {
                    var captured = false
                    children.toList().reversed().forEach {
                        if (it is EffectEditViewNew) {
                            if (!captured && it.tryInterceptTouchEvent(ev, it.outerCenterPoint)) {
//                                Log.e("===", "${this.javaClass.name} onInterceptTouchEvent ${ev.x} ${ev.y}")
                                captured = true
                                it.editing = true
                                editingViewNew = it

                                if (selectViewNew != it) {
                                    if (selectViewNew != null) {
                                        log("${selectViewNew!!.id} unselected")
                                        (selectViewNew!!.contentView as ImageView).setImageDrawable(
                                            null
                                        )
                                    }
                                    selectViewNew = it
                                    log("${selectViewNew!!.id} selected")
                                    (selectViewNew!!.contentView as ImageView).setImageDrawable(
                                        drawable
                                    )
                                }
                            } else {
                                it.editing = false
                            }
                        }
                    }
                    if (!captured) {
                        editingViewNew = null
                        if (selectViewNew != null) {
                            log("${selectViewNew!!.id} unselected")
                            (selectViewNew!!.contentView as ImageView).setImageDrawable(null)
                        }
                        selectViewNew = null
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                editingViewNew = null
            }
        }
        return false
    }
}

fun Any.log(log: String) {
    Log.d("===", log)
}