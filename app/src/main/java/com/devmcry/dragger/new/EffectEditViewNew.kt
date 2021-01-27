package com.devmcry.dragger.new

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import com.devmcry.dragger.RotationGestureDetector
import com.devmcry.dragger.multitouch.MoveGestureDetector
import kotlin.math.hypot

/**
 *  @author : DevMcryYu
 *  @date : 1/21/21 7:16 PM
 *  @description :
 */
class EffectEditViewNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseEditView(context, attrs, defStyleAttr) {
//
//
//    var isMoving = false
//    private val moveGestureDetector by lazy {
//        MoveGestureDetector(context,
//            object : MoveGestureDetector.SimpleOnMoveGestureListener() {
//                override fun onMove(detector: MoveGestureDetector): Boolean {
//                    offsetLeftAndRight(detector.focusDelta.x.toInt())
//                    offsetTopAndBottom(detector.focusDelta.y.toInt())
//                    return super.onMove(detector)
//                }
//
//                override fun onMoveBegin(detector: MoveGestureDetector?): Boolean {
//                    isMoving = true
//                    return super.onMoveBegin(detector)
//                }
//
//                override fun onMoveEnd(detector: MoveGestureDetector?) {
//                    isMoving = false
//                    super.onMoveEnd(detector)
//                }
//            })
//    }
//
//    var isScaling = false
//    private val scaleGestureDetector by lazy {
//        ScaleGestureDetector(
//            context,
//            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                override fun onScale(detector: ScaleGestureDetector): Boolean {
//                    val newScale = (detector.currentSpan / detector.previousSpan)
//                    Log.d("===", "${this@EffectEditViewNew.id} scale $newScale")
//                    scale = newScale
//                    return true
//                }
//
//                override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
//                    isScaling = true
//                    return super.onScaleBegin(detector)
//                }
//
//                override fun onScaleEnd(detector: ScaleGestureDetector?) {
//                    isScaling = false
//                    super.onScaleEnd(detector)
//                }
//            }).apply {
//            isQuickScaleEnabled = false
//        }
//    }
//
//    private val rotationGestureDetector by lazy {
//        RotationGestureDetector(object :
//            RotationGestureDetector.SimpleOnRotationGestureListener() {
//            override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
//                val delta = rotationDetector.angle
//                Log.d("===", "${this@EffectEditViewNew.id} rotate $delta")
//                angle += delta
//                return super.onRotation(rotationDetector)
//            }
//        })
//    }
//
//    init {
//        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//    }
//
//    private var centerPoint: Point? = null
//    private val preTouchPoint: Point by lazy { Point(0, 0) }
//    private var preDistance: Int = 0
//    private var preAngle: Float = 0f
//    private val curTouchPoint: Point by lazy { Point(0, 0) }
//    private var curDistance: Int = 0
//    private var curAngle: Float = 0f
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (editing) {
////            when (event.actionMasked) {
////                MotionEvent.ACTION_DOWN -> {
////                    bringToFront()
////                    if (currentEditType != null) {
////                        Log.d("===", "ACTION_DOWN")
////                        preTouchPoint.x = event.x.toInt()
////                        preTouchPoint.y = event.y.toInt()
////                        centerPoint = innerCenterPoint
////                        preDistance = calculateDistance(preTouchPoint, centerPoint!!)
////                        preAngle = calculateAngle(preTouchPoint, centerPoint!!)
////                        Log.d("===", "ACTION_DOWN $preTouchPoint")
////                    }
////                }
////                MotionEvent.ACTION_MOVE -> {
////                    if (currentEditType != null) {
////                        Log.d("===", "ACTION_MOVE")
////                        curTouchPoint.x = event.x.toInt()
////                        curTouchPoint.y = event.y.toInt()
////                        curDistance = calculateDistance(curTouchPoint, centerPoint!!)
////                        curAngle = calculateAngle(curTouchPoint, centerPoint!!)
//////
////                        val deltaAngle = toDegrees(preAngle - curAngle.toDouble())
////                        Log.d("===", "ACTION_MOVE $centerPoint!!")
////                        Log.d("===", "ACTION_MOVE deltaAngle $deltaAngle")
//////                        angle +=deltaAngle.toFloat()
////                        val scale =
////                            (1f * curDistance / preDistance).coerceAtLeast(0.95f)
////                                .coerceAtMost(1.05f)
//////                        setScale(scale)
////                        Log.d("===", "ACTION_MOVE scale $scale")
////                    }
////                }
////                MotionEvent.ACTION_UP -> {
////                    if (currentEditType != null) {
////                        Log.d("===", "ACTION_UP")
////                        currentEditType = null
////                        preTouchPoint.set(0, 0)
////                        curTouchPoint.set(0, 0)
////                    }
////                }
////            }
//            if (currentEditType == null) {
//                if (event.pointerCount == 1) {
//                    moveGestureDetector.onTouchEvent(event)
//                }
//            }
//            scaleGestureDetector.onTouchEvent(event)
//            rotationGestureDetector.onTouchEvent(event)
//            return true
//        } else {
//            return false
//        }
//    }
}