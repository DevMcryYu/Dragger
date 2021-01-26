package com.devmcry.dragger.new

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.devmcry.dragger.PosTransHelper
import kotlin.math.atan2
import kotlin.math.hypot

/**
 *  @author : DevMcryYu
 *  @date : 1/26/21 11:43 AM
 *  @description :
 */
open class BaseEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var contentView: View?
        get() = if (childCount > 0) getChildAt(0) else null
        set(value) {
            if (value != null) {
                if (childCount > 1) {
                    removeAllViews()
                }
                originWidth = value.layoutParams.width
                originHeight = value.layoutParams.height
                addView(value)
            } else {
                removeAllViews()
            }
        }

    var angle: Float
        get() = rotation
        set(value) {
            rotation = value
            invalidate()
        }

    var size: IntArray
        get() = intArrayOf(measuredWidth, measuredHeight)
        set(value) {
            contentView?.updateLayoutParams {
                width = value[0]
                height = value[1]
            }
        }

    private var originWidth: Int = 0
    private var originHeight: Int = 0

    var scale: Float
        get() = 1f * width / originWidth
        set(value) {
            contentView?.updateLayoutParams {
                width = (measuredWidth * value).toInt()
                height = (measuredHeight * value).toInt()
            }
        }

    init {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        this.setWillNotDraw(false)
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

    protected val radius = 56

    protected val editButtonList: List<EditType> by lazy {
        listOf(
            EditType.Delete,
            EditType.Edit,
            EditType.Adjust,
            EditType.Custom,
        )
    }
    var currentEditType: EditType? = null

    var editing = false
        set(value) {
            field = value
            invalidate()
        }

    val innerCenterPoint get() = Point(measuredWidth / 2, measuredHeight / 2)

    private val paint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 1f
    }

    private val adjustPaint: Paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 1f
    }


    private val linePath = Path()
    private val linePaint: Paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (editing) {
            // draw lines
            linePath.reset()
            getEditPoints(calculateRotate = false).forEachIndexed { index, point ->
                if (index == 0) {
                    linePath.moveTo(point.x.toFloat(), point.y.toFloat())
                } else {
                    linePath.lineTo(point.x.toFloat(), point.y.toFloat())
                }
            }
            linePath.close()
            canvas?.drawPath(linePath, linePaint)
            // draw buttons
            getEditPoints(calculateRotate = false).forEachIndexed { index, point ->
                when (editButtonList[index]) {
                    EditType.Adjust -> {
                        canvas?.drawCircle(
                            point.x.toFloat(),
                            point.y.toFloat(),
                            radius.toFloat(),
                            adjustPaint
                        )
                    }
                    else -> {
                        canvas?.drawCircle(
                            point.x.toFloat(),
                            point.y.toFloat(),
                            radius.toFloat(),
                            paint
                        )
                    }
                }
            }
        }
    }

    fun tryInterceptTouchEvent(event: MotionEvent, centerPoint: Point): Boolean {
        // edit buttons or content view
        return isEditButtonUnder(event, centerPoint) || isContentViewUnder(event, centerPoint)
    }

    private fun isEditButtonUnder(event: MotionEvent, centerPoint: Point): Boolean {
        var result = false
        getEditPoints(centerPoint)
            .forEachIndexed { index, point ->
                val touchPoint = Point(event.x.toInt(), event.y.toInt())
                val distance = calculateDistance(touchPoint, point)
                if (distance < radius) {
                    result = true
                    currentEditType = editButtonList[index]
                    Log.d("===", "${this.id} ${editButtonList[index]} touch")
                }
            }
        return result
    }

    /**
     * 只需要判断该点是否在上下两边和左右两边之间即可
     *   p1            p2
     *    ┏━━━━━━━━━━━┓
     *    ┃           ┃   ● p
     *    ┃           ┃
     *    ┗━━━━━━━━━━━┛
     *   p4            p3
     *
     * (p1 p2 X p1 p ) * (p3 p4 X p3 p)  >= 0  && (p2 p3 X p2 p ) * (p4 p1 X p4 p) >= 0
     */
    private fun isContentViewUnder(event: MotionEvent, centerPoint: Point): Boolean {
        fun getCross(firstPoint: Point, secondPoint: Point, targetPoint: Point): Float {
            return 1f * (secondPoint.x - firstPoint.x) * (targetPoint.y - firstPoint.y) - 1f * (targetPoint.x - firstPoint.x) * (secondPoint.y - firstPoint.y)
        }

        val point = Point(event.x.toInt(), event.y.toInt())
        val (p1, p2, p3, p4) = getEditPoints(centerPoint)
        val result = getCross(p1, p2, point) * getCross(p3, p4, point) >= 0 &&
                getCross(p2, p3, point) * getCross(p4, p1, point) >= 0
        if (result) {
            currentEditType = null
            Log.d("===", "${this.id} content touch")
        }
        return result
    }

    private fun getEditPoints(
        centerPoint: Point = this.innerCenterPoint,
        calculateRotate: Boolean = true
    ): List<Point> {
        return editButtonList.map {
            when (it) {
                // top left
                EditType.Delete -> {
                    Point(
                        centerPoint.x - contentView!!.measuredWidth / 2,
                        centerPoint.y - contentView!!.measuredHeight / 2
                    )
                }
                // top right
                EditType.Edit -> {
                    Point(
                        centerPoint.x + contentView!!.measuredWidth / 2,
                        centerPoint.y - contentView!!.measuredHeight / 2
                    )
                }
                // bottom right
                EditType.Adjust -> {
                    Point(
                        centerPoint.x + contentView!!.measuredWidth / 2,
                        centerPoint.y + contentView!!.measuredHeight / 2
                    )
                }
                // bottom left
                EditType.Custom -> {
                    Point(
                        centerPoint.x - contentView!!.measuredWidth / 2,
                        centerPoint.y + contentView!!.measuredHeight / 2
                    )
                }
            }
        }.map {
            if (calculateRotate) {
                PosTransHelper.rotatePoint(it, rotation, centerPoint)
            } else {
                it
            }
        }
    }

    private fun calculateDistance(touchPoint: Point, targetPoint: Point): Int {
        return hypot(
            touchPoint.x.toFloat() - targetPoint.x.toFloat(),
            touchPoint.y.toFloat() - targetPoint.y.toFloat()
        ).toInt()
    }

    private fun calculateAngle(startPoint: Point, targetPoint: Point): Float {
        return atan2(startPoint.x - targetPoint.x.toFloat(), startPoint.y - targetPoint.y.toFloat())
    }

    enum class EditType {
        Delete,
        Edit,
        Adjust,
        Custom
    }
}