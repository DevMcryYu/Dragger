package com.devmcry.dragger

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.customview.widget.ViewDragHelper

/**
 *  @author : DevMcryYu
 *  @date : 1/21/21 2:51 PM
 *  @description :
 */
class DecorContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val viewDragHelper: ViewDragHelper by lazy {
        ViewDragHelper.create(
            this,
            1f,
            viewDragCallback
        )
    }

    private val rotationGestureDetector by lazy {
        RotationGestureDetector(object :
            RotationGestureDetector.SimpleOnRotationGestureListener() {
            override fun onRotation(rotationDetector: RotationGestureDetector?): Boolean {
                rotationDetector?.angle?.let { angle ->
                    Log.d("==== rotate angle", angle.toString())
                    viewDragHelper.capturedView?.run {
                        (this as FrameLayout).getChildAt(0).rotation += angle
//                        ObjectAnimator.ofFloat(
//                            this,
//                            View.ROTATION,
//                            this.rotation,
//                            this.rotation + angle
//                        ).setDuration(1).start()
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

                        viewDragHelper.capturedView?.run {
                            (this as FrameLayout).getChildAt(0).scaleX = scale
                            (this as FrameLayout).getChildAt(0).scaleY = scale
//                            AnimatorSet().apply {
//                                playTogether(
//                                    ObjectAnimator.ofFloat(
//                                        this@run,
//                                        View.SCALE_X,
//                                        this@run.scaleX,
//                                        scale
//                                    ),
//                                    ObjectAnimator.ofFloat(
//                                        this@run,
//                                        View.SCALE_Y,
//                                        this@run.scaleY,
//                                        scale
//                                    )
//                                )
//                                duration = 1
//                                start()
//                            }
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

    override fun onFinishInflate() {
        super.onFinishInflate()

    }

    private val viewDragCallback: ViewDragHelper.Callback by lazy {
        object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean = true

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                bringChildToFront(capturedChild)
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
//                viewDragHelper.processTouchEvent(event)
            } else {
                viewDragHelper.cancel()
            }
        }.onFailure {
            result = false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return result
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            invalidate()
        }
    }

}