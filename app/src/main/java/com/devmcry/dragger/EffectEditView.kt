package com.devmcry.dragger

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import com.devmcry.dragger.multitouch.MoveGestureDetector
import java.lang.Math.toDegrees
import kotlin.math.atan2
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
    private val radius = 56
    var editing = false
        set(value) {
            field = value
            invalidate()
        }
    private val editButtonList: List<EditType> by lazy {
        listOf(
            EditType.Delete,
            EditType.Edit,
            EditType.Adjust,
            EditType.Custom,
        )
    }
    private var currentEditType: EditType? = null

    private val diagonal: Int
        get() = hypot(
            measuredWidth.toFloat(),
            measuredHeight.toFloat()
        ).toInt()
    val innerCenterPoint get() = Point(measuredWidth / 2, measuredHeight / 2)
    val outerCenterPoint
        get() = innerCenterPoint.apply {
            x += left
            y += top
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

    var isMoving = false
    private val moveGestureDetector by lazy {
        MoveGestureDetector(context,
            object : MoveGestureDetector.SimpleOnMoveGestureListener() {
                override fun onMove(detector: MoveGestureDetector): Boolean {
                    offsetLeftAndRight(detector.focusDelta.x.toInt())
                    offsetTopAndBottom(detector.focusDelta.y.toInt())
                    return super.onMove(detector)
                }

                override fun onMoveBegin(detector: MoveGestureDetector?): Boolean {
                    isMoving = true
                    return super.onMoveBegin(detector)
                }

                override fun onMoveEnd(detector: MoveGestureDetector?) {
                    isMoving = false
                    super.onMoveEnd(detector)
                }
            })
    }

    var isScaling = false
    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val newScale = (detector.currentSpan / detector.previousSpan)
                    Log.d("===", "${this@EffectEditView.id} scale $newScale")
                    setScale(newScale)
                    return true
                }

                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                    isScaling = true
                    return super.onScaleBegin(detector)
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {
                    isScaling = false
                    super.onScaleEnd(detector)
                }
            }).apply {
            isQuickScaleEnabled = false
        }
    }

    private val rotationGestureDetector by lazy {
        RotationGestureDetector(object :
            RotationGestureDetector.SimpleOnRotationGestureListener() {
            override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
                val delta = rotationDetector.angle
                Log.d("===", "${this@EffectEditView.id} rotate $delta")
                angle += delta
                return super.onRotation(rotationDetector)
            }
        })
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
        val animator = if (id == R.id.test) {
            ValueAnimator.ofFloat(0f, 135f)
        } else {
            ValueAnimator.ofFloat(0f, -75f)
        }
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            angle = value
        }
        animator.start()
    }
    //test

    private var centerPoint: Point? = null
    private val preTouchPoint: Point by lazy { Point(0, 0) }
    private var preDistance: Int = 0
    private var preAngle: Float = 0f
    private val curTouchPoint: Point by lazy { Point(0, 0) }
    private var curDistance: Int = 0
    private var curAngle: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (editing) {
//            when (event.actionMasked) {
//                MotionEvent.ACTION_DOWN -> {
//                    bringToFront()
//                    if (currentEditType != null) {
//                        Log.d("===", "ACTION_DOWN")
//                        preTouchPoint.x = event.x.toInt()
//                        preTouchPoint.y = event.y.toInt()
//                        centerPoint = innerCenterPoint
//                        preDistance = calculateDistance(preTouchPoint, centerPoint!!)
//                        preAngle = calculateAngle(preTouchPoint, centerPoint!!)
//                        Log.d("===", "ACTION_DOWN $preTouchPoint")
//                    }
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    if (currentEditType != null) {
//                        Log.d("===", "ACTION_MOVE")
//                        curTouchPoint.x = event.x.toInt()
//                        curTouchPoint.y = event.y.toInt()
//                        curDistance = calculateDistance(curTouchPoint, centerPoint!!)
//                        curAngle = calculateAngle(curTouchPoint, centerPoint!!)
////
//                        val deltaAngle = toDegrees(preAngle - curAngle.toDouble())
//                        Log.d("===", "ACTION_MOVE $centerPoint!!")
//                        Log.d("===", "ACTION_MOVE deltaAngle $deltaAngle")
////                        angle +=deltaAngle.toFloat()
//                        val scale =
//                            (1f * curDistance / preDistance).coerceAtLeast(0.95f)
//                                .coerceAtMost(1.05f)
////                        setScale(scale)
//                        Log.d("===", "ACTION_MOVE scale $scale")
//                    }
//                }
//                MotionEvent.ACTION_UP -> {
//                    if (currentEditType != null) {
//                        Log.d("===", "ACTION_UP")
//                        currentEditType = null
//                        preTouchPoint.set(0, 0)
//                        curTouchPoint.set(0, 0)
//                    }
//                }
//            }
            if (currentEditType == null) {
                if (event.pointerCount == 1) {
                    moveGestureDetector.onTouchEvent(event)
                }
            }
            scaleGestureDetector.onTouchEvent(event)
            rotationGestureDetector.onTouchEvent(event)
            return true
        } else {
            return false
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (editing) {
            // draw lines
            linePath.reset()
            getEditPoints().forEachIndexed { index, point ->
                if (index == 0) {
                    linePath.moveTo(point.x.toFloat(), point.y.toFloat())
                } else {
                    linePath.lineTo(point.x.toFloat(), point.y.toFloat())
                }
            }
            linePath.close()
            canvas?.drawPath(linePath, linePaint)
            // draw buttons
            getEditPoints().forEachIndexed { index, point ->
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
        getEditPoints(centerPoint).forEachIndexed { index, point ->
            val touchPoint = Point(event.x.toInt(), event.y.toInt())
            val distance = calculateDistance(touchPoint, point)
            if (distance < radius) {
                result = true
                currentEditType = editButtonList[index]
                Log.d("===", "${this@EffectEditView.id} ${editButtonList[index]} touch")
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
            Log.d("===", "${this@EffectEditView.id} content touch")
        }
        return result
    }

    private fun getEditPoints(centerPoint: Point = this.innerCenterPoint): List<Point> {
        return editButtonList.map {
            when (it) {
                // top left
                EditType.Delete -> {
                    Point(
                        centerPoint.x - contentView.measuredWidth / 2,
                        centerPoint.y - contentView.measuredHeight / 2
                    )
                }
                // top right
                EditType.Edit -> {
                    Point(
                        centerPoint.x + contentView.measuredWidth / 2,
                        centerPoint.y - contentView.measuredHeight / 2
                    )
                }
                // bottom right
                EditType.Adjust -> {
                    Point(
                        centerPoint.x + contentView.measuredWidth / 2,
                        centerPoint.y + contentView.measuredHeight / 2
                    )
                }
                // bottom left
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