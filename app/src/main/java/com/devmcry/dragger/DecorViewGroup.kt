package com.devmcry.dragger

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children

/**
 *  @author : DevMcryYu
 *  @date : 1/22/21 12:13 PM
 *  @description :
 */
class DecorViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var editingView: EffectEditView? = null
    private val drawable by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_launcher_foreground,
            context.theme
        )
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view = EffectEditView(context).apply {
            id = 1
            val content = ImageView(context)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            setSize(500, 600)
            angle = 76f
        }
        addView(view)
        val view1 = EffectEditView(context).apply {
            id = 2
            val content = ImageView(context)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            setSize(400, 300)
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
                editingView = null
            }
        }
//        Log.e("===", "${this.javaClass.name} onTouchEvent ${event.x} ${event.y}")
        if (editingView != null) {
            editingView?.dispatchTouchEvent(event)
        }
        return true
    }

    private var selectView: EffectEditView? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (editingView == null) {
                    var captured = false
                    children.toList().reversed().forEach {
                        if (it is EffectEditView) {
                            if (!captured && it.tryInterceptTouchEvent(ev, it.outerCenterPoint)) {
//                                Log.e("===", "${this.javaClass.name} onInterceptTouchEvent ${ev.x} ${ev.y}")
                                captured = true
                                it.editing = true
                                editingView = it

                                if (selectView != it) {
                                    if (selectView != null) {
                                        log("${selectView!!.id} unselected")
                                        (selectView!!.contentView as ImageView).setImageDrawable(
                                            null
                                        )
                                    }
                                    selectView = it
                                    log("${selectView!!.id} selected")
                                    (selectView!!.contentView as ImageView).setImageDrawable(
                                        drawable
                                    )
                                }
                            } else {
                                it.editing = false
                            }
                        }
                    }
                    if (!captured) {
                        editingView = null
                        if (selectView != null) {
                            log("${selectView!!.id} unselected")
                            (selectView!!.contentView as ImageView).setImageDrawable(null)
                        }
                        selectView = null
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                editingView = null
            }
        }
        return false
    }
}

fun Any.log(log: String) {
    Log.d("===", log)
}