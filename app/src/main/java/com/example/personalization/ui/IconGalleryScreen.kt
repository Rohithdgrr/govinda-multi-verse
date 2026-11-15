package com.example.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personalization.icons.IconRegistry
import com.example.personalization.icons.PersonalizationIcon
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.IconStyleFamily
import com.example.personalization.theme.LocalGovindaTokens

private val iconTintSwatches = listOf(
    null,
    0xFF003DA5,
    0xFF0969DA,
    0xFF4FC1FF,
    0xFFBD93F9,
    0xFF50FA7B,
    0xFFFABD2F,
    0xFFE24329
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconGallerySection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredGlyphs = remember(searchQuery, selectedCategory) {
        IconRegistry.filterGlyphs(searchQuery, selectedCategory)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Icon Style Pack (8 Styles)",
            subtitle = "Choose vector geometry applied across GOVINDA controls and glyphs."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconRegistry.families.forEach { family ->
                    StudioOptionChip(
                        label = family.displayName,
                        selected = draft.iconProfile.family == family,
                        onClick = {
                            onUpdateDraft {
                                it.copy(
                                    iconProfile = it.iconProfile.copy(
                                        family = family,
                                        navIconFamily = family,
                                        categoryIconFamily = family
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("icon_family_${family.name.lowercase()}")
                    )
                }
            }

            StudioSliderRow(
                label = "Icon Size",
                valueText = "${draft.iconProfile.sizeDp.toInt()} dp",
                value = draft.iconProfile.sizeDp,
                valueRange = 16f..32f,
                onValueChange = { sz ->
                    onUpdateDraft { it.copy(iconProfile = it.iconProfile.copy(sizeDp = sz)) }
                }
            )

            StudioSliderRow(
                label = "Stroke Width",
                valueText = "${"%.1f".format(draft.iconProfile.strokeWidthDp)} dp",
                value = draft.iconProfile.strokeWidthDp,
                valueRange = 1.2f..3.4f,
                onValueChange = { stroke ->
                    onUpdateDraft { it.copy(iconProfile = it.iconProfile.copy(strokeWidthDp = stroke)) }
                }
            )

            Text(
                text = "ICON TINT COLOR",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioOptionChip(
                    label = "Theme Accent",
                    selected = draft.iconProfile.customTintArgb == null,
                    onClick = {
                        onUpdateDraft { it.copy(iconProfile = it.iconProfile.copy(customTintArgb = null)) }
                    }
                )
                iconTintSwatches.filterNotNull().forEach { argb ->
                    ColorSwatchCircle(
                        colorArgb = argb,
                        selected = draft.iconProfile.customTintArgb == argb,
                        onClick = {
                            onUpdateDraft { it.copy(iconProfile = it.iconProfile.copy(customTintArgb = argb)) }
                        }
                    )
                }
            }
        }

        StudioSectionCard(
            title = "Local Vector Icon Gallery (${filteredGlyphs.size})",
            subtitle = "Search and inspect all math & system icons rendered offline."
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search math or UI glyphs…") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = tokens.primary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.primary,
                    unfocusedBorderColor = tokens.border,
                    focusedTextColor = tokens.primaryText,
                    unfocusedTextColor = tokens.primaryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("icon_gallery_search")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconRegistry.categories.forEach { cat ->
                    StudioOptionChip(
                        label = cat,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredGlyphs.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { glyph ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tokens.background,
                                border = BorderStroke(1.2.dp, tokens.border),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PersonalizationIcon(
                                        glyph = glyph,
                                        iconProfile = draft.iconProfile,
                                        selected = true
                                    )
                                    Text(
                                        text = glyph.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = tokens.primaryText,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        repeat(3 - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
