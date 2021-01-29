package com.devmcry.dragger.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.updateLayoutParams
import com.devmcry.dragger.R
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

    companion object {
        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 2f
    }

    val diagonal: Int
        get() = hypot(
            size[0].toFloat(),
            size[1].toFloat()
        ).toInt()

    private val innerCenterPoint get() = Point(measuredWidth / 2, measuredHeight / 2)

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

    private val radius = 24.dp

    private val iconDelete: Bitmap? by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.icon_edit_delete,
            context.theme
        )?.toBitmap(radius, radius)
    }

    private val iconAdjust: Bitmap? by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.icon_edit_adjust,
            context.theme
        )?.toBitmap(radius, radius)
    }

    // 通过调整 enable 来开启
    private val editButtonList: List<EditType> by lazy {
        listOf(
            EditType.Delete,
            EditType.Edit.apply { enable = false },
            EditType.Adjust,
            EditType.Custom.apply { enable = false }
        )
    }
    var currentEditType: EditType? = null

    var editing = false
        set(value) {
            field = value
            invalidate()
        }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        isDither = true
    }

    private val linePath = Path()
    private val linePaint: Paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        isDither = true
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
                val button = editButtonList[index]
                if (button.enable) {
                    when (button) {
                        EditType.Adjust -> {
                            iconAdjust?.let {
                                canvas?.drawBitmap(
                                    it,
                                    point.x.toFloat() - it.width / 2f,
                                    point.y.toFloat() - it.height / 2f,
                                    iconPaint
                                )
                            }
                        }
                        else -> {
                            iconDelete?.let {
                                canvas?.drawBitmap(
                                    it,
                                    point.x.toFloat() - it.width / 2f,
                                    point.y.toFloat() - it.height / 2f,
                                    iconPaint
                                )
                            }
                        }
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
        }
        return currentEditType != null || contentViewUnder
    }

    /**
     * 判断落点与四个顶点的距离
     */
    fun isEditButtonUnder(event: MotionEvent, centerPoint: Point): EditType? {
        var result: EditType? = null
        kotlin.run breaking@{
            getEditPoints(centerPoint)
                .forEachIndexed { index, point ->
                    val button = editButtonList[index]
                    if (button.enable) {
                        val touchPoint = Point(event.x.toInt(), event.y.toInt())
                        val distance = calculateDistance(touchPoint, point)
                        if (distance < radius) {
                            result = button
                            return@breaking
                        }
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

    // 在这里调整按钮顺序
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

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    private val Int.sp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
}