package com.example.personalization.avatars

import com.example.personalization.model.AvatarBadge
import com.example.personalization.model.AvatarBorderStyle
import com.example.personalization.model.AvatarProfile
import com.example.personalization.model.AvatarStyle
import kotlin.random.Random

object AvatarRegistry {

    val presets: List<AvatarProfile> = listOf(
        AvatarProfile(
            seed = 108,
            style = AvatarStyle.GEOMETRIC,
            shapeVariant = 0,
            backgroundColorArgb = 0xFFEAF1FF,
            accentColorArgb = 0xFF003DA5,
            borderStyle = AvatarBorderStyle.SOLID,
            badge = AvatarBadge.SIGMA,
            monogram = "G"
        ),
        AvatarProfile(
            seed = 256,
            style = AvatarStyle.ABSTRACT_TECH,
            shapeVariant = 1,
            backgroundColorArgb = 0xFF1E1E1E,
            accentColorArgb = 0xFF4FC1FF,
            borderStyle = AvatarBorderStyle.DOUBLE_TECH,
            badge = AvatarBadge.CODE,
            monogram = "01"
        ),
        AvatarProfile(
            seed = 314,
            style = AvatarStyle.MATH_CHARACTER,
            shapeVariant = 2,
            backgroundColorArgb = 0xFFFDF6E3,
            accentColorArgb = 0xFF268BD2,
            borderStyle = AvatarBorderStyle.SOLID,
            badge = AvatarBadge.PI,
            monogram = "π"
        ),
        AvatarProfile(
            seed = 512,
            style = AvatarStyle.PIXEL,
            shapeVariant = 3,
            backgroundColorArgb = 0xFF282A36,
            accentColorArgb = 0xFFBD93F9,
            borderStyle = AvatarBorderStyle.DASHED,
            badge = AvatarBadge.BOLT,
            monogram = "P"
        ),
        AvatarProfile(
            seed = 777,
            style = AvatarStyle.MINIMAL_ILLUSTRATED,
            shapeVariant = 0,
            backgroundColorArgb = 0xFFECEFF4,
            accentColorArgb = 0xFF5E81AC,
            borderStyle = AvatarBorderStyle.SOLID,
            badge = AvatarBadge.ROOT,
            monogram = "M"
        ),
        AvatarProfile(
            seed = 900,
            style = AvatarStyle.GRADIENT_FREE,
            shapeVariant = 2,
            backgroundColorArgb = 0xFFF4EFE4,
            accentColorArgb = 0xFFB84A39,
            borderStyle = AvatarBorderStyle.DOUBLE_TECH,
            badge = AvatarBadge.NONE,
            monogram = "B"
        )
    )

    val avatarColorSwatches: List<Long> = listOf(
        0xFFEAF1FF,
        0xFFF4F7FE,
        0xFFFDF6E3,
        0xFFECEFF4,
        0xFF1E1E1E,
        0xFF282A36,
        0xFF0D1117,
        0xFF002B36
    )

    val accentSwatches: List<Long> = listOf(
        0xFF003DA5,
        0xFF1647BB,
        0xFF0969DA,
        0xFF268BD2,
        0xFF4FC1FF,
        0xFFBD93F9,
        0xFF50FA7B,
        0xFFFABD2F,
        0xFFE24329
    )

    fun generateFromSeed(seed: Int, current: AvatarProfile): AvatarProfile {
        val rng = Random(seed)
        val styles = AvatarStyle.entries
        val borders = AvatarBorderStyle.entries
        val badges = AvatarBadge.entries
        return current.copy(
            seed = seed,
            style = styles[rng.nextInt(styles.size)],
            shapeVariant = rng.nextInt(4),
            backgroundColorArgb = avatarColorSwatches[rng.nextInt(avatarColorSwatches.size)],
            accentColorArgb = accentSwatches[rng.nextInt(accentSwatches.size)],
            accessoryIndex = rng.nextInt(4),
            borderStyle = borders[rng.nextInt(borders.size)],
            badge = badges[rng.nextInt(badges.size)]
        )
    }
}
