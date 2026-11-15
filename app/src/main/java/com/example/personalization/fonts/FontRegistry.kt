package com.example.personalization.fonts

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.personalization.model.FontCategory
import com.example.personalization.model.FontOption

object FontRegistry {

    val allFonts: List<FontOption> = listOf(
        // Condensed
        FontOption("barlow_condensed", "Barlow Condensed", FontCategory.CONDENSED),
        FontOption("roboto_condensed", "Roboto Condensed", FontCategory.CONDENSED),
        FontOption("oswald", "Oswald", FontCategory.CONDENSED),
        FontOption("archivo_narrow", "Archivo Narrow", FontCategory.CONDENSED),
        // Sans-serif
        FontOption("inter", "Inter", FontCategory.SANS_SERIF),
        FontOption("roboto", "Roboto", FontCategory.SANS_SERIF),
        FontOption("lato", "Lato", FontCategory.SANS_SERIF),
        FontOption("open_sans", "Open Sans", FontCategory.SANS_SERIF),
        FontOption("nunito_sans", "Nunito Sans", FontCategory.SANS_SERIF),
        FontOption("manrope", "Manrope", FontCategory.SANS_SERIF),
        FontOption("poppins", "Poppins", FontCategory.SANS_SERIF),
        FontOption("montserrat", "Montserrat", FontCategory.SANS_SERIF),
        // Monospace
        FontOption("ibm_plex_mono", "IBM Plex Mono", FontCategory.MONOSPACE),
        FontOption("jetbrains_mono", "JetBrains Mono", FontCategory.MONOSPACE),
        FontOption("fira_code", "Fira Code", FontCategory.MONOSPACE),
        FontOption("source_code_pro", "Source Code Pro", FontCategory.MONOSPACE),
        FontOption("roboto_mono", "Roboto Mono", FontCategory.MONOSPACE),
        // Serif
        FontOption("lora", "Lora", FontCategory.SERIF),
        FontOption("merriweather", "Merriweather", FontCategory.SERIF),
        FontOption("noto_serif", "Noto Serif", FontCategory.SERIF)
    )

    private val familyCache = mutableMapOf<String, FontFamily>()

    fun fontFamilyFor(id: String): FontFamily {
        return familyCache.getOrPut(id) {
            val resId = when (id) {
                "barlow_condensed" -> R.font.barlow_condensed
                "roboto_condensed" -> R.font.roboto_condensed
                "oswald" -> R.font.oswald
                "archivo_narrow" -> R.font.archivo_narrow
                "inter" -> R.font.inter
                "roboto" -> R.font.roboto
                "lato" -> R.font.lato
                "open_sans" -> R.font.open_sans
                "nunito_sans" -> R.font.nunito_sans
                "manrope" -> R.font.manrope
                "poppins" -> R.font.poppins
                "montserrat" -> R.font.montserrat
                "ibm_plex_mono" -> R.font.ibm_plex_mono
                "jetbrains_mono" -> R.font.jetbrains_mono
                "fira_code" -> R.font.fira_code
                "source_code_pro" -> R.font.source_code_pro
                "roboto_mono" -> R.font.roboto_mono
                "lora" -> R.font.lora
                "merriweather" -> R.font.merriweather
                "noto_serif" -> R.font.noto_serif
                else -> R.font.ibm_plex_mono
            }
            // Keep Normal weight so Compose FontSynthesis thickens Medium/SemiBold/Bold cleanly
            FontFamily(Font(resId, FontWeight.Normal))
        }
    }

    fun findById(id: String): FontOption =
        allFonts.firstOrNull { it.id == id } ?: allFonts.first()
}
