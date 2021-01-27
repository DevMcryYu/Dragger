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

    private val innerCenterPoint get() = Point(measuredWidth / 2, measuredHeight / 2)
    val diagonal: Int
        get() = hypot(
            size[0].toFloat(),
            size[1].toFloat()
        ).toInt()
    val outerCenterPoint
        get() = innerCenterPoint.apply {
            x += left
            y += top
        }

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

    private var originWidth: Int = 0
    private var originHeight: Int = 0

    companion object {
        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 1.5f
    }

    /* transform */
    var angle: Float
        get() = rotation
        set(value) {
            rotation = value
            requestLayout()
            invalidate()
        }

    var scale: Float = 1f
        set(value) {
            field = value.coerceAtLeast(MIN_SCALE).coerceAtMost(MAX_SCALE)
            val newWidth = (originWidth * field).toInt()
            val newHeight = (originHeight * field).toInt()
            size = intArrayOf(newWidth, newHeight)
        }

    var size: IntArray
        get() = intArrayOf(
            contentView?.layoutParams?.width ?: 0,
            contentView?.layoutParams?.height ?: 0
        )
        set(value) {
            contentView?.updateLayoutParams {
                val newWidth = value[0].coerceAtLeast((originWidth * MIN_SCALE).toInt())
                    .coerceAtMost((originWidth * MAX_SCALE).toInt())
                val newHeight = value[1].coerceAtLeast((originHeight * MIN_SCALE).toInt())
                    .coerceAtMost((originHeight * MAX_SCALE).toInt())
                offsetLeftAndRight((width - newWidth) / 2)
                offsetTopAndBottom((height - newHeight) / 2)
                width = newWidth
                height = newHeight
            }
            invalidate()
            requestLayout()
        }
    /* transform */

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
        var contentViewUnder = false
        currentEditType = isEditButtonUnder(event, centerPoint)
        // 如果未命中顶点再判断是否命中内部区域
        if (currentEditType == null) {
            contentViewUnder = isContentViewUnder(event, centerPoint)
            if (contentViewUnder) {
                Log.d("===", "${this.id} content touch")
            }
        }
        return currentEditType != null || contentViewUnder
    }

    fun isEditButtonUnder(event: MotionEvent, centerPoint: Point): EditType? {
        var result: EditType? = null
        kotlin.run breaking@{
            getEditPoints(centerPoint)
                .forEachIndexed { index, point ->
                    val touchPoint = Point(event.x.toInt(), event.y.toInt())
                    val distance = calculateDistance(touchPoint, point)
                    if (distance < radius) {
                        result = editButtonList[index]
                        return@breaking
                    }
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
    fun isContentViewUnder(event: MotionEvent, centerPoint: Point): Boolean {
        fun getCross(firstPoint: Point, secondPoint: Point, targetPoint: Point): Float {
            return 1f * (secondPoint.x - firstPoint.x) * (targetPoint.y - firstPoint.y) - 1f * (targetPoint.x - firstPoint.x) * (secondPoint.y - firstPoint.y)
        }

        val point = Point(event.x.toInt(), event.y.toInt())
        val (p1, p2, p3, p4) = getEditPoints(centerPoint)
        val result = getCross(p1, p2, point) * getCross(p3, p4, point) >= 0 &&
                getCross(p2, p3, point) * getCross(p4, p1, point) >= 0
        return result
    }

    private fun getEditPoints(
        centerPoint: Point = this.innerCenterPoint,
        calculateRotate: Boolean = true
    ): List<Point> {
        val contentSize = size
        return editButtonList.map {
            when (it) {
                // top left
                EditType.Delete -> {
                    Point(
                        centerPoint.x - contentSize[0] / 2,
                        centerPoint.y - contentSize[1] / 2
                    )
                }
                // top right
                EditType.Edit -> {
                    Point(
                        centerPoint.x + contentSize[0] / 2,
                        centerPoint.y - contentSize[1] / 2
                    )
                }
                // bottom right
                EditType.Adjust -> {
                    Point(
                        centerPoint.x + contentSize[0] / 2,
                        centerPoint.y + contentSize[1] / 2
                    )
                }
                // bottom left
                EditType.Custom -> {
                    Point(
                        centerPoint.x - contentSize[0] / 2,
                        centerPoint.y + contentSize[1] / 2
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

    fun calculateDistance(touchPoint: Point, targetPoint: Point): Int {
        return hypot(
            touchPoint.x.toFloat() - targetPoint.x.toFloat(),
            touchPoint.y.toFloat() - targetPoint.y.toFloat()
        ).toInt()
    }

    fun calculateAngle(startPoint: Point, targetPoint: Point): Float {
        return atan2(startPoint.y - targetPoint.y.toFloat(), startPoint.x - targetPoint.x.toFloat())
    }
}