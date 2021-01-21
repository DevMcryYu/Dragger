package com.devmcry.dragger

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.children


/**
 *  @author : DevMcryYu
 *  @date : 1/21/21 3:35 PM
 *  @description :
 */
class DecorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var lastX = 0
    private var lastY = 0

    private val rotationGestureDetector by lazy {
        RotationGestureDetector(object :
            RotationGestureDetector.SimpleOnRotationGestureListener() {
            override fun onRotation(rotationDetector: RotationGestureDetector?): Boolean {
                rotationDetector?.angle?.let { angle ->
                    Log.d("==== rotate angle", angle.toString())
                    this@DecorView.rotation += angle
                }
                return super.onRotation(rotationDetector)
            }
        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = true

        kotlin.runCatching {
            rotationGestureDetector.onTouchEvent(event)
        }.onFailure {
            result = false
        }

        val action = event.action
        //获取手机触摸的坐标
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount <= 1) {
                    val offsetX: Int = x - lastX
                    val offsetY: Int = y - lastY
                    layout(
                        left + offsetX, top + offsetY,
                        right + offsetX, bottom + offsetY
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return result
    }

}