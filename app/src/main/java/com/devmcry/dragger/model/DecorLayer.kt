package com.devmcry.dragger.model

import android.graphics.Bitmap

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/18 16:52
 *  @description : 用于将贴纸等装饰物输出到 OpenGL 进行渲染
 */

const val GLOBAL_DEFAULT_DELAY = 30
const val TIME_START = 0
const val TIME_INFINITE = -1
const val DEFAULT_LEVEL = 1

data class DecorLayer(
    val vertex: FloatArray = FloatArray(8) { 0f },
    val frames: MutableList<DecorFrame> = mutableListOf(),
    var type: DynamicTypes = DynamicTypes.PNGS,
    var pack: String = "",
    var res: String = "",
    var file: String = "",
    var defaultDelay: Int = GLOBAL_DEFAULT_DELAY,
    var startTimeMillis: Int = TIME_START,
    var endTimeMillis: Int = TIME_INFINITE,
    var level: Int = DEFAULT_LEVEL
) {
    data class DecorFrame(
        val index: Int = 0,
        val bitmap: Bitmap?,  // 选填，如果 bitmap 为null，可通过 file 获取
        val file: String,  // 必填，用于获取 bitmap。如果 bitmap 来源自网络图片，file 填写图片网络地址；如果 bitmap 为组图中的一帧，file 为图地址 + # + 序号
        val delay: Int = GLOBAL_DEFAULT_DELAY
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecorLayer

        if (!vertex.contentEquals(other.vertex)) return false
        if (startTimeMillis != other.startTimeMillis) return false
        if (endTimeMillis != other.endTimeMillis) return false
        if (level != other.level) return false
        if (frames != other.frames) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertex.contentHashCode()
        result = 31 * result + startTimeMillis.hashCode()
        result = 31 * result + endTimeMillis.hashCode()
        result = 31 * result + level
        result = 31 * result + frames.hashCode()
        return result
    }
}