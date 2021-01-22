package com.devmcry.dragger

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
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
        if (editingView != null) {
            editingView?.onTouchEvent(event)
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                var captured = false
                children.toList().reversed().forEach {
                    if (it is EffectEditView) {
                        val centerPoint =
                            Point(it.left + it.measuredWidth / 2, it.top + it.measuredHeight / 2)
                        if (!captured && it.tryInterceptTouchEvent(ev, centerPoint)) {
                            captured = true
                            it.editing = true
                            editingView = it
                        } else {
                            it.editing = false
                        }
                    }
                }
            }
        }
        return true
    }
}