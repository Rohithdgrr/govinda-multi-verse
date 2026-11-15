package com.example.personalization.icons

import com.example.personalization.model.IconGlyph
import com.example.personalization.model.IconStyleFamily

object IconRegistry {
    val families: List<IconStyleFamily> = IconStyleFamily.entries
    val allGlyphs: List<IconGlyph> = IconGlyph.entries
    val categories: List<String> = listOf("All") + allGlyphs.map { it.categoryGroup }.distinct()

    fun filterGlyphs(query: String, category: String): List<IconGlyph> {
        val q = query.trim().lowercase()
        return allGlyphs.filter { glyph ->
            val catMatch = category == "All" || glyph.categoryGroup.equals(category, ignoreCase = true)
            val queryMatch = q.isEmpty() ||
                glyph.displayName.lowercase().contains(q) ||
                glyph.categoryGroup.lowercase().contains(q)
            catMatch && queryMatch
        }
    }
}
