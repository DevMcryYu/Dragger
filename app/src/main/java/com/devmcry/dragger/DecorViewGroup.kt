package com.devmcry.dragger

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
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
) : ConstraintLayout(context, attrs, defStyleAttr) {


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            children.iterator().forEach {
                if (it is EffectEditView) {
                    val centerPoint =
                        Point(it.left + it.measuredWidth / 2, it.top + it.measuredHeight / 2)
                    if (it.tryInterceptTouchEvent(ev, centerPoint)) {
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}