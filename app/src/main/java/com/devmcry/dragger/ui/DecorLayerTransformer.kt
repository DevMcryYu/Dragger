package com.devmcry.dragger.ui

import android.graphics.Point
import android.graphics.PointF
import androidx.core.graphics.toPointF
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/18 17:51
 *  @description :
 */
interface DecorLayerTransformer<T> {

    fun save(): List<Pair<BaseEditView, PointF>>

    fun restore(decorList: List<Pair<BaseEditView, PointF>>)

    fun export(): List<T>
}

object PosTransHelper {

    fun transToCenterX(viewX: Int, totalX: Int): Float = (viewX / totalX.toFloat() - 0.5f) * 2f

    fun transToCenterY(viewY: Int, totalY: Int): Float = (0.5f - viewY / totalY.toFloat()) * 2f

    fun transToViewX(centerX: Float, totalX: Int): Int =
        ((centerX * 0.5f + 0.5f) * totalX).roundToInt()

    fun transToViewY(centerY: Float, totalY: Int): Int =
        ((0.5 - centerY * 0.5f) * totalY).roundToInt()

    fun transToCenter(point: Point, centerPoint: Point) = PointF().apply {
        x = transToCenterX(point.x, centerPoint.x * 2)
        y = transToCenterY(point.y, centerPoint.y * 2)
    }

    fun transToView(pointF: PointF, centerPoint: Point) = Point().apply {
        x = transToViewX(pointF.x, centerPoint.x * 2)
        y = transToViewY(pointF.y, centerPoint.y * 2)
    }

    fun rotatePoint(
        point: Point,
        rotation: Float,
        centerPoint: Point = Point(0, 0),
        clockwise: Boolean = true
    ) =
        rotatePoint(point.toPointF(), rotation, centerPoint.toPointF(), clockwise).roundToPoint()

    fun rotatePoint(
        pointF: PointF,
        rotation: Float,
        centerPoint: PointF = PointF(0f, 0f),
        clockwise: Boolean = true
    ) =
        PointF().apply {
            val angle = if (rotation < 0) {
                (360.0 + rotation) % 360.0
            } else {
                rotation.toDouble() % 360.0
            }
            val rad = toRadians(angle).toFloat()

            x = pointF.x - centerPoint.x
            y = pointF.y - centerPoint.y

            val newX: Float
            val newY: Float
            if (clockwise) {
                newX = x * cos(rad) - y * sin(rad)
                newY = x * sin(rad) + y * cos(rad)
            } else {
                newX = x * cos(rad) + y * sin(rad)
                newY = -x * sin(rad) + y * cos(rad)
            }

            x = newX + centerPoint.x
            y = newY + centerPoint.y
        }

    /**
     * 先转换到目标坐标系，然后进行中心旋转
     */
    fun pointToVertex(
        viewPoint: Point,
        viewCenterPoint: Point,
        angle: Float,
        centerPoint: Point
    ): FloatArray {
        val rotatedPoint = rotatePoint(viewPoint, angle, viewCenterPoint)
        val transRotatedPoint = transToCenter(rotatedPoint, centerPoint)
        return floatArrayOf(transRotatedPoint.x, transRotatedPoint.y)
    }

    fun viewToVertex(view: BaseEditView, centerPoint: Point): FloatArray {
        val angle = view.angle
        val size = view.size
        val vp0 = Point(view.left + size[0], view.top)
        val vp1 = Point(view.left + size[0], view.top + size[1])
        val vp2 = Point(view.left, view.top + size[1])
        val vp3 = Point(view.left, view.top)

        val viewCenterPoint = Point(
            (view.left + size[0] / 2f).roundToInt(),
            (view.top + size[1] / 2f).roundToInt()
        )
        return pointToVertex(vp0, viewCenterPoint, angle, centerPoint) +
                pointToVertex(vp1, viewCenterPoint, angle, centerPoint) +
                pointToVertex(vp2, viewCenterPoint, angle, centerPoint) +
                pointToVertex(vp3, viewCenterPoint, angle, centerPoint)
    }

    private fun PointF.roundToPoint() = Point().apply {
        this.x = this@roundToPoint.x.roundToInt()
        this.y = this@roundToPoint.y.roundToInt()
    }
}