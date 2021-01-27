package com.devmcry.dragger.new

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.customview.widget.ViewDragHelper
import com.devmcry.dragger.R
import com.devmcry.dragger.RotationGestureDetector
import java.lang.Math.toDegrees

/**
 *  @author : DevMcryYu
 *  @date : 1/26/21 12:23 PM
 *  @description :
 */
open class BaseEditViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    protected var selectViewNew: DecorEditView? = null
    private val viewDragHelper: ViewDragHelper by lazy {
        ViewDragHelper.create(
            this,
            1f,
            viewDragCallback
        )
    }

    private val drawable by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_launcher_foreground,
            context.theme
        )
    }

    private val viewDragCallback: ViewDragHelper.Callback by lazy {
        object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean = true

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                capturedChild.bringToFront()
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
                        val scaleDelta = (curSpan / preSpan) - 1
                        log("scaleDelta $scaleDelta")
                        selectViewNew?.run {
                            this.scale += scaleDelta
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
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                var captured = false
                children.toList().reversed().forEach {
                    if (it is DecorEditView) {
                        if (!captured && it.tryInterceptTouchEvent(event, it.outerCenterPoint)) {
                            captured = true
                            it.editing = true
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
                    if (selectViewNew != null) {
                        log("${selectViewNew!!.id} unselected")
                        (selectViewNew!!.contentView as ImageView).setImageDrawable(null)
                    }
                    selectViewNew = null
                }
            }
        }
        return true
    }

    private val curTouchPoint: Point by lazy { Point(0, 0) }
    private var curDistance: Int = 0
    private var preAngle: Float = 0f
    private var curAngle: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = true
        kotlin.runCatching {
            if (selectViewNew != null) {
                if (selectViewNew?.currentEditType == null) {
                    if (event.pointerCount == 1) {
                        viewDragHelper.processTouchEvent(event)
                        // 解决 ViewDragHelper ACTION_DOWN 事件时调用 cancel 导致 mActivePointerId 被重置的问题
                        // 该问题导致 captureChildView 后 isValidPointerForActionMove 返回 false 拖动失败
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                            selectViewNew?.run {
                                if (isContentViewUnder(event, outerCenterPoint)) {
                                    viewDragHelper.captureChildView(this, event.getPointerId(0))
                                }
                            }
                        }
                    } else {
                        viewDragHelper.cancel()
                    }
                    scaleGestureDetector.onTouchEvent(event)
                    rotationGestureDetector.onTouchEvent(event)
                } else {
                    // 只允许单指操控
                    if (event.pointerCount == 1) {
                        val type = requireNotNull(selectViewNew?.currentEditType)
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                when (type) {
                                    EditType.Adjust -> {
                                        selectViewNew?.run {
                                            curTouchPoint.x = event.x.toInt()
                                            curTouchPoint.y = event.y.toInt()
                                        }
                                    }
                                }
                            }
                            MotionEvent.ACTION_MOVE -> {
                                when (type) {
                                    EditType.Adjust -> {
                                        selectViewNew?.run {
                                            preAngle =
                                                calculateAngle(curTouchPoint, outerCenterPoint)
                                            curTouchPoint.x = event.x.toInt()
                                            curTouchPoint.y = event.y.toInt()
                                            // 先旋转
                                            curAngle =
                                                calculateAngle(curTouchPoint, outerCenterPoint)
                                            val deltaAngle =
                                                -toDegrees(preAngle - curAngle.toDouble())
                                            angle += deltaAngle.toFloat()
                                            // 后缩放
                                            curDistance =
                                                calculateDistance(curTouchPoint, outerCenterPoint)
                                            val scaleDelta =
                                                (1f * curDistance / (diagonal / 2f)) - 1
                                            this.scale += scaleDelta
                                        }
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP -> {
                                val newType = selectViewNew?.let {
                                    it.isEditButtonUnder(event, it.outerCenterPoint)
                                }
                                log("type: $type newType $newType")
                                when (type) {
                                    EditType.Delete -> {
                                        if (type == newType) {
                                            log("delete click")
                                        }
                                    }
                                    EditType.Edit -> {
                                        if (type == newType) {
                                            log("edit click")
                                        }
                                    }
                                    EditType.Custom -> {
                                        if (type == newType) {
                                            log("custom click")
                                        }
                                    }
                                    EditType.Adjust -> {
                                        selectViewNew?.run {
                                            curTouchPoint.set(0, 0)
                                            curDistance = 0
                                            curAngle = 0f
                                            preAngle = 0f
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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