package com.devmcry.dragger.model

enum class DynamicTypes(val type: Int, val aliasName: String) {
    PNGS(1, "pngs"),            // png组图
    DOT9S(3, "dot9s"),          // Android特有点9图
    ALPHAMP4(4, "alphamp4"),    // 透明mp4
    FRAME(5, "frame"),          // 边框，不同尺寸给不通的png序列
    SPRITE(6, "sprite")         // png精灵图
}