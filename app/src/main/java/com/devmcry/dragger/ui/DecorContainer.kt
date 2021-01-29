package com.devmcry.dragger.ui

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.devmcry.dragger.R
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
) : BaseEditViewContainer(context, attrs, defStyleAttr), DecorLayerTransformer<Any> {
    var onResize: (List<Any>) -> Unit = {}
    var onSelect: (Any) -> Unit = {}
    var onUnSelect: (Any, Boolean) -> Unit = { _, _ -> }

    private val centerPoint: Point
        get() = Point(
            (this.width / 2f).roundToInt(),
            (this.height / 2f).roundToInt()
        )

    private val decorViewMap: MutableMap<BaseEditView, Any> by lazy { mutableMapOf() }

    private val drawable by lazy {
        ResourcesCompat.getDrawable(
            resources,
            R.color.teal_200,
            context.theme
        )
    }

    override fun onSelected(view: BaseEditView) {
        (view.contentView as ImageView).setImageDrawable(drawable)
        decorViewMap[view]?.let {
            onSelect(it)
        }
    }

    override fun onUnSelected(view: BaseEditView) {
        if (!isEditing) {
            Toast.makeText(context, "all clear", Toast.LENGTH_SHORT).show()
        }
        decorViewMap[view]?.let {
            onUnSelect(it, !isEditing)
        }
        (view.contentView as ImageView).setImageDrawable(null)
    }

    override fun onAction(view: BaseEditView, type: EditType) {
        Toast.makeText(context, "$type click", Toast.LENGTH_SHORT).show()
    }


    private fun newChildPos(size: IntArray): IntArray {
        return intArrayOf((this.width - size[0]) / 2, (this.height - size[1]) / 2)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        post {
            kotlin.runCatching {
                val v = DecorEditView(context).apply {
                    val content = ImageView(context)
                    content.layoutParams = LayoutParams(400, 500)
                    background = ResourcesCompat.getDrawable(
                        resources,
                        R.color.teal_700,
                        context.theme
                    )
                    contentView = content
                    angle = 0f
                    val pos = newChildPos(size)
                    left = pos[0]
                    top = pos[1]
                }
                addView(v)
            }
        }
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
        post { onResize(export()) }
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

    override fun export(): List<Any> {
        return decorViewMap.entries
            .sortedByDescending { indexOfChild(it.key) }
            .mapIndexed { index, entry ->
                entry.value
            }.toList()
    }
}