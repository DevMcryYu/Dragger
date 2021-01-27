package com.devmcry.dragger.new

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.devmcry.dragger.DecorLayerTransformer
import com.devmcry.dragger.PosTransHelper
import com.devmcry.dragger.R
import com.devmcry.dragger.model.DecorLayer
import kotlin.math.roundToInt

/**
 *  @author : DevMcryYu
 *  @date : 1/22/21 12:13 PM
 *  @description :
 */
class DecorContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseEditViewContainer(context, attrs, defStyleAttr), DecorLayerTransformer {

    private val drawable by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_launcher_foreground,
            context.theme
        )
    }

    override fun onSelected(view: BaseEditView) {
        (view.contentView as ImageView).setImageDrawable(drawable)
    }

    override fun onUnSelected(view: BaseEditView) {
        (view.contentView as ImageView).setImageDrawable(null)
    }

    override fun onAction(view: BaseEditView, type: EditType) {
        Toast.makeText(context, "$type click", Toast.LENGTH_SHORT).show()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val view = DecorEditView(context).apply {
            val content = ImageView(context)
            content.layoutParams = LayoutParams(300, 600)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_700, context.theme)
            contentView = content
            angle = 0f
        }
        addView(view)
        val view1 = DecorEditView(context).apply {
            val content = ImageView(context)
            content.layoutParams = LayoutParams(400, 400)
            content.background =
                ResourcesCompat.getDrawable(resources, R.color.teal_200, context.theme)
            contentView = content
            angle = 0f
        }
        addView(view1)
    }

    fun setSize(width: Int, height: Int) {
        val pointList = save()
        if (layoutParams != null) {
            layoutParams.width = width
            layoutParams.height = height
        } else {
            layoutParams = LayoutParams(width, height)
        }
        restore(pointList)
    }

    override fun save(): List<Pair<BaseEditView, PointF>> {
        val saveList = mutableListOf<Pair<BaseEditView, PointF>>()
        val dstCenterPoint = Point((this.width / 2f).roundToInt(), (this.height / 2f).roundToInt())
        children.iterator().forEach {
            if (it is BaseEditView) {
                val viewPoint = Point(it.left, it.top)
                val transPoint = PosTransHelper.transToCenter(viewPoint, dstCenterPoint)
                val pair = Pair(it, transPoint)
                saveList.add(pair)
            }
        }
        return saveList
    }

    override fun restore(decorList: List<Pair<BaseEditView, PointF>>) {
        val dstCenterPoint = Point(
            (this.layoutParams.width / 2f).roundToInt(),
            (this.layoutParams.height / 2f).roundToInt()
        )
        decorList.forEach {
            val view = it.first
            val viewPoint = PosTransHelper.transToView(it.second, dstCenterPoint)
            view.left = viewPoint.x
            view.top = viewPoint.y
        }
        requestLayout()
    }

    override fun export(): List<DecorLayer> {
        val dstCenterPoint = Point((this.width / 2f).roundToInt(), (this.height / 2f).roundToInt())
        return children
            .filter { it is BaseEditView }
            .map { it as BaseEditView }
            .mapIndexed { index, view ->
                DecorLayer().apply {
                    level = index
                    pack = "sticker1"
                    PosTransHelper.viewToVertex(view, dstCenterPoint).copyInto(vertex)
                }
            }.toList()
    }

}