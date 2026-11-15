package com.example.personalization.model

import androidx.compose.runtime.Immutable

enum class AvatarStyle(val displayName: String) {
    GEOMETRIC("Geometric"),
    ABSTRACT_TECH("Abstract Tech"),
    MINIMAL_ILLUSTRATED("Minimal Face"),
    PIXEL("Pixel Grid"),
    MATH_CHARACTER("Math Symbol"),
    DEVELOPER("Dev Terminal"),
    MONOGRAM("Monogram"),
    GRADIENT_FREE("Flat Bauhaus")
}

enum class AvatarBorderStyle(val displayName: String) {
    NONE("None"),
    SOLID("Solid"),
    DOUBLE_TECH("Double Blueprint"),
    DASHED("Dashed")
}

enum class AvatarBadge(val displayName: String, val symbol: String) {
    NONE("None", ""),
    PI("Pi", "π"),
    SIGMA("Sigma", "Σ"),
    BOLT("Bolt", "⚡"),
    ROOT("Root", "√"),
    CODE("Code", "</>")
}

@Immutable
data class AvatarProfile(
    val seed: Int = 108,
    val style: AvatarStyle = AvatarStyle.GEOMETRIC,
    val shapeVariant: Int = 0,
    val backgroundColorArgb: Long = 0xFFEAF1FF,
    val accentColorArgb: Long = 0xFF003DA5,
    val accessoryIndex: Int = 0,
    val borderStyle: AvatarBorderStyle = AvatarBorderStyle.SOLID,
    val badge: AvatarBadge = AvatarBadge.SIGMA,
    val monogram: String = "G"
)
