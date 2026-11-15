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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalization.fonts.FontRegistry
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.FontCategory
import com.example.personalization.model.FontWeightLevel
import com.example.personalization.theme.LocalGovindaTokens

private enum class TypographyTargetRole(val label: String) {
    HEADING("Heading Font"),
    BODY("Body Font"),
    EQUATION("Equation Font"),
    TECHNICAL("Technical Font")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypographyStudioSection(
    draft: AppearanceProfile,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    var targetRole by remember { mutableStateOf(TypographyTargetRole.EQUATION) }
    var selectedCategory by remember { mutableStateOf<FontCategory?>(null) }

    val selectedFontId = when (targetRole) {
        TypographyTargetRole.HEADING -> draft.typography.headingFontId
        TypographyTargetRole.BODY -> draft.typography.bodyFontId
        TypographyTargetRole.EQUATION -> draft.typography.equationFontId
        TypographyTargetRole.TECHNICAL -> draft.typography.technicalFontId
    }

    val filteredFonts = remember(selectedCategory) {
        if (selectedCategory == null) FontRegistry.allFonts
        else FontRegistry.allFonts.filter { it.category == selectedCategory }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Live Typography Specimen
        StudioSectionCard(
            title = "Live Equation & Text Specimen",
            subtitle = "Bundled offline fonts — instant preview with tabular numerals."
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.background, RoundedCornerShape(6.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "GOVINDA ENGINE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = tokens.primary
                )
                Text(
                    text = "3/4 + 1/8 = 7/8  (√144 × 2)",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = (26f * draft.typography.fontSizeScale).sp),
                    color = tokens.primaryText
                )
                Text(
                    text = "Offline adaptive arithmetic challenge formatting with 0123456789 alignment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.secondaryText
                )
                Text(
                    text = "HEAD: ${FontRegistry.findById(draft.typography.headingFontId).displayName} · EQ: ${FontRegistry.findById(draft.typography.equationFontId).displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.mutedText
                )
            }
        }

        StudioSectionCard(
            title = "Select Font Family (20 Offline Fonts)",
            subtitle = "Assign fonts independently to headings, body, equations, and labels."
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TypographyTargetRole.entries.forEach { role ->
                    StudioOptionChip(
                        label = role.label,
                        selected = targetRole == role,
                        onClick = { targetRole = role }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StudioOptionChip(
                    label = "All (20)",
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
                FontCategory.entries.forEach { cat ->
                    StudioOptionChip(
                        label = cat.displayName,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                filteredFonts.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { font ->
                            val isSelected = font.id == selectedFontId
                            Surface(
                                onClick = {
                                    onUpdateDraft { prof ->
                                        val typo = when (targetRole) {
                                            TypographyTargetRole.HEADING -> prof.typography.copy(headingFontId = font.id)
                                            TypographyTargetRole.BODY -> prof.typography.copy(bodyFontId = font.id)
                                            TypographyTargetRole.EQUATION -> prof.typography.copy(equationFontId = font.id)
                                            TypographyTargetRole.TECHNICAL -> prof.typography.copy(technicalFontId = font.id)
                                        }
                                        prof.copy(typography = typo)
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) tokens.selectedState else tokens.background,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.3.dp,
                                    color = if (isSelected) tokens.primary else tokens.border
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("font_option_${font.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = font.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = FontRegistry.fontFamilyFor(font.id),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) tokens.primary else tokens.primaryText,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${font.category.displayName} · 12 + 34",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontRegistry.fontFamilyFor(font.id)
                                        ),
                                        color = tokens.secondaryText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        StudioSectionCard(
            title = "Type Scale, Weight & Numerals",
            subtitle = "Fine-tune global text sizing, stroke weight, spacing & monospace digits."
        ) {
            StudioSliderRow(
                label = "Font Size Scale",
                valueText = "${"%.2f".format(draft.typography.fontSizeScale)}x",
                value = draft.typography.fontSizeScale,
                valueRange = 0.85f..1.25f,
                onValueChange = { scale ->
                    onUpdateDraft { it.copy(typography = it.typography.copy(fontSizeScale = scale)) }
                }
            )

            Text(
                text = "FONT WEIGHT",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FontWeightLevel.entries.forEach { level ->
                    StudioOptionChip(
                        label = level.label,
                        selected = draft.typography.fontWeightLevel == level,
                        onClick = {
                            onUpdateDraft { it.copy(typography = it.typography.copy(fontWeightLevel = level)) }
                        }
                    )
                }
            }

            StudioSliderRow(
                label = "Letter Spacing",
                valueText = "${"%.2f".format(draft.typography.letterSpacingScale)}x",
                value = draft.typography.letterSpacingScale,
                valueRange = 0.6f..1.5f,
                onValueChange = { ls ->
                    onUpdateDraft { it.copy(typography = it.typography.copy(letterSpacingScale = ls)) }
                }
            )

            StudioSliderRow(
                label = "Line Height",
                valueText = "${"%.2f".format(draft.typography.lineHeightScale)}x",
                value = draft.typography.lineHeightScale,
                valueRange = 0.9f..1.3f,
                onValueChange = { lh ->
                    onUpdateDraft { it.copy(typography = it.typography.copy(lineHeightScale = lh)) }
                }
            )

            StudioToggleRow(
                title = "Tabular Numerals (tnum)",
                subtitle = "Keep digits fixed-width for clean arithmetic alignment",
                checked = draft.typography.tabularNumerals,
                onCheckedChange = { checked ->
                    onUpdateDraft { it.copy(typography = it.typography.copy(tabularNumerals = checked)) }
                }
            )
        }
    }
}
