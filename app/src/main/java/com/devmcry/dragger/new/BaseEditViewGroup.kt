package com.devmcry.dragger.new

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.customview.widget.ViewDragHelper
import com.devmcry.dragger.RotationGestureDetector

/**
 *  @author : DevMcryYu
 *  @date : 1/26/21 12:23 PM
 *  @description :
 */
open class BaseEditViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    protected var selectViewNew: EffectEditViewNew? = null
    private val viewDragHelper: ViewDragHelper by lazy {
        ViewDragHelper.create(
            this,
            1f,
            viewDragCallback
        )
    }

    private val viewDragCallback: ViewDragHelper.Callback by lazy {
        object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean = true

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            }

            override fun onViewPositionChanged(
                changedView: View,
                left: Int,
                top: Int,
                dx: Int,
                dy: Int
            ) {
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int = top

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = left

            override fun getViewHorizontalDragRange(child: View): Int = 1

            override fun getViewVerticalDragRange(child: View): Int = 1
        }
    }

    private val rotationGestureDetector by lazy {
        RotationGestureDetector(object :
            RotationGestureDetector.SimpleOnRotationGestureListener() {
            override fun onRotation(rotationDetector: RotationGestureDetector?): Boolean {
                rotationDetector?.angle?.let { angle ->
                    Log.d("==== rotate angle", angle.toString())
                    selectViewNew?.run {
                        this.angle += angle
                    }
                }
                return super.onRotation(rotationDetector)
            }
        })
    }

    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    return if (detector != null) {
                        val preSpan = detector.previousSpan
                        val curSpan = detector.currentSpan
                        val scale = (curSpan / preSpan)
                        selectViewNew?.run {
                            this.scale = scale
                        }
                        true
                    } else {
                        false
                    }
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {
                }

                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                    if (viewDragHelper.capturedView != null) {
                        viewDragHelper.cancel()
                    }
                    return true
                }
            }).apply {
            isQuickScaleEnabled = false

        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        children.iterator().forEach {
            it.layout(it.left, it.top, it.left + it.measuredWidth, it.top + it.measuredHeight)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return viewDragHelper.shouldInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = true
        kotlin.runCatching {
            result = scaleGestureDetector.onTouchEvent(event)
            rotationGestureDetector.onTouchEvent(event)
            if (event.pointerCount == 1) {
                viewDragHelper.processTouchEvent(event)
            } else {
                viewDragHelper.cancel()
            }
        }.onFailure {
            result = false
        }

        return result
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            invalidate()
        }
    }
}