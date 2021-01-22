package com.devmcry.dragger

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import kotlin.math.hypot

/**
 *  @author : DevMcryYu
 *  @date : 1/21/21 7:16 PM
 *  @description :
 */
class EffectEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RotateLayout(context, attrs, defStyleAttr) {
    val radius = 96
    private val editButtonList: List<EditType> by lazy {
        listOf(
            EditType.Delete,
            EditType.Edit,
            EditType.Adjust,
            EditType.Custom,
        )
    }

    private val diagonal: Int
        get() = hypot(
            measuredWidth.toFloat(),
            measuredHeight.toFloat()
        ).toInt()

    val centerPoint get() = Point(measuredWidth / 2, measuredHeight / 2)

    val paint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 1f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        kotlin.runCatching {
            parent as ViewGroup
        }.onSuccess {
            if (it.clipChildren) {
                it.clipChildren = false
            }
            if (it.clipToPadding) {
                it.clipToPadding = false
            }
        }
    }

    //test
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (id == R.id.test) {
            val animator = ValueAnimator.ofFloat(0f, 45f)
            animator.duration = 1000
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
//                val lp = view.layoutParams
//                lp.width += 1
//                lp.height += 1
//                view.layoutParams = lp
                angle = value
            }
            animator.start()
        }
        contentView.setOnClickListener {
            this.bringToFront()
            Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
        }
    }
    //test

    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        when (event?.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                handleEditButtonTouch(event)
//                return true
//            }
//        }
        return super.onTouchEvent(event)
    }


    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        getEditPoints(centerPoint).forEach {
            canvas?.drawCircle(it.x.toFloat(), it.y.toFloat(), radius.toFloat(), paint)
        }
    }

    fun tryInterceptTouchEvent(event: MotionEvent, centerPoint: Point): Boolean {
        var result = false
        getEditPoints(centerPoint).forEachIndexed { index, point ->
            val touchPoint = Point(event.x.toInt(), event.y.toInt())
            val distance = calculateDistance(touchPoint, point)
            Log.d("====", "distance $distance")
            if (distance < radius) {
                result = true
                Toast.makeText(context, "${editButtonList[index]} click", Toast.LENGTH_SHORT).show()
            }
        }
        return result
    }


    fun getEditPoints(centerPoint: Point): List<Point> {
        return editButtonList.map {
            when (it) {
                EditType.Delete -> {
                    Point(
                        centerPoint.x - contentView.measuredWidth / 2,
                        centerPoint.y - contentView.measuredHeight / 2
                    )
                }
                EditType.Edit -> {
                    Point(
                        centerPoint.x + contentView.measuredWidth / 2,
                        centerPoint.y - contentView.measuredHeight / 2
                    )
                }
                EditType.Adjust -> {
                    Point(
                        centerPoint.x + contentView.measuredWidth / 2,
                        centerPoint.y + contentView.measuredHeight / 2
                    )
                }
                EditType.Custom -> {
                    Point(
                        centerPoint.x - contentView.measuredWidth / 2,
                        centerPoint.y + contentView.measuredHeight / 2
                    )
                }
            }
        }.map {
            PosTransHelper.rotatePoint(it, angle, centerPoint)
        }
    }

    private fun handleEditButtonTouch(event: MotionEvent) {
        val touchPoint = Point(event.x.toInt(), event.y.toInt())
        var editType: EditType? = null
        getEditPoints(centerPoint).forEachIndexed { index, point ->
            val dst = calculateDistance(touchPoint, point)
            if (dst < radius) {
                editType = editButtonList[index]
            }
        }
        if (editType != null) {
            Toast.makeText(context, "${editType?.name} clicked", Toast.LENGTH_SHORT).show()
        }
    }

    fun calculateDistance(touchPoint: Point, targetPoint: Point): Int {
        return hypot(
            touchPoint.x.toFloat() - targetPoint.x.toFloat(),
            touchPoint.y.toFloat() - targetPoint.y.toFloat()
        ).toInt()
    }

    enum class EditType {
        Delete,
        Edit,
        Adjust,
        Custom
    }
}