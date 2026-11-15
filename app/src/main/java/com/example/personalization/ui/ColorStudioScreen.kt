package com.example.personalization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalization.model.AppearanceProfile
import com.example.personalization.model.CustomColorOverrides
import com.example.personalization.model.ThemeDefinition
import com.example.personalization.theme.LocalGovindaTokens
import com.example.personalization.theme.ThemeManager
import com.example.personalization.theme.ThemeRegistry

private enum class ColorSlot(val label: String) {
    PRIMARY("Primary"),
    ACCENT("Accent"),
    BACKGROUND("Background"),
    SURFACE("Surface"),
    TEXT("Primary Text"),
    BORDER("Border")
}

private data class QuickPalettePreset(
    val name: String,
    val primary: Long,
    val accent: Long,
    val bg: Long,
    val surface: Long,
    val text: Long,
    val border: Long
)

private val quickPalettes = listOf(
    QuickPalettePreset("Cobalt Ink", 0xFF003DA5, 0xFF0055E5, 0xFFFFFFFF, 0xFFF4F7FE, 0xFF0E295E, 0xFF7CA2E8),
    QuickPalettePreset("Emerald Lab", 0xFF0D7A5F, 0xFF10B981, 0xFFF7FCFA, 0xFFECF7F3, 0xFF0A3328, 0xFF7EC9B2),
    QuickPalettePreset("Crimson Draft", 0xFFB8293D, 0xFFE11D48, 0xFFFFFAFA, 0xFFFCF0F2, 0xFF3E1019, 0xFFE598A4),
    QuickPalettePreset("Cyber Neon", 0xFF38BDF8, 0xFFF472B6, 0xFF0F172A, 0xFF1E293B, 0xFFF8FAFC, 0xFF334155),
    QuickPalettePreset("Amber Terminal", 0xFFF59E0B, 0xFFFBBF24, 0xFF121212, 0xFF1C1C1C, 0xFFFEF3C7, 0xFF574419)
)

private val colorSwatches = listOf(
    0xFF003DA5, 0xFF0969DA, 0xFF1E66F5, 0xFF4FC1FF,
    0xFF7759C2, 0xFFBD93F9, 0xFF0D7A5F, 0xFF50FA7B,
    0xFFB84A39, 0xFFFABD2F, 0xFFFFFFFF, 0xFFFDF6E3,
    0xFFF4F7FE, 0xFFECEFF4, 0xFF1E1E1E, 0xFF0D1117,
    0xFF000000, 0xFF0E295E, 0xFFF8F8F2, 0xFF7CA2E8
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorStudioSection(
    draft: AppearanceProfile,
    customThemes: List<ThemeDefinition>,
    onUpdateDraft: ((AppearanceProfile) -> AppearanceProfile) -> Unit,
    onSaveCustomTheme: (String, Boolean) -> Unit,
    onRenameCustomTheme: (String, String) -> Unit,
    onDeleteCustomTheme: (String) -> Unit
) {
    val tokens = LocalGovindaTokens.current
    val systemDark = isSystemInDarkTheme()
    val resolved = ThemeManager.resolveActiveTheme(draft, customThemes, systemDark)

    var selectedSlot by remember { mutableStateOf(ColorSlot.PRIMARY) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<ThemeDefinition?>(null) }

    val activeSlotArgb = when (selectedSlot) {
        ColorSlot.PRIMARY -> draft.customColors.primaryArgb ?: resolved.primaryArgb
        ColorSlot.ACCENT -> draft.customColors.accentArgb ?: resolved.accentArgb
        ColorSlot.BACKGROUND -> draft.customColors.backgroundArgb ?: resolved.backgroundArgb
        ColorSlot.SURFACE -> draft.customColors.surfaceArgb ?: resolved.surfaceArgb
        ColorSlot.TEXT -> draft.customColors.primaryTextArgb ?: resolved.primaryTextArgb
        ColorSlot.BORDER -> draft.customColors.borderArgb ?: resolved.borderArgb
    }

    fun applySlotArgb(argb: Long) {
        onUpdateDraft { prof ->
            val cc = prof.customColors.copy(enabled = true)
            val updated = when (selectedSlot) {
                ColorSlot.PRIMARY -> cc.copy(primaryArgb = argb)
                ColorSlot.ACCENT -> cc.copy(accentArgb = argb)
                ColorSlot.BACKGROUND -> cc.copy(backgroundArgb = argb)
                ColorSlot.SURFACE -> cc.copy(surfaceArgb = argb)
                ColorSlot.TEXT -> cc.copy(primaryTextArgb = argb)
                ColorSlot.BORDER -> cc.copy(borderArgb = argb)
            }
            prof.copy(customColors = updated)
        }
    }

    val contrast = ThemeManager.contrastRatio(resolved.primaryTextArgb, resolved.backgroundArgb)
    val contrastLabel = when {
        contrast >= 7.0f -> "AAA (${"%.1f".format(contrast)}:1)"
        contrast >= 4.5f -> "AA (${"%.1f".format(contrast)}:1)"
        else -> "LOW (${"%.1f".format(contrast)}:1)"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudioSectionCard(
            title = "Custom Color Overrides",
            subtitle = "Customize accent, background, surface, text & border colors live.",
            trailing = {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (contrast >= 4.5f) tokens.success.copy(alpha = 0.16f) else tokens.error.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = contrastLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (contrast >= 4.5f) tokens.success else tokens.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        ) {
            StudioToggleRow(
                title = "Enable Custom Color Overrides",
                subtitle = "Layer custom colors on top of the active base theme",
                checked = draft.customColors.enabled,
                onCheckedChange = { enabled ->
                    onUpdateDraft { it.copy(customColors = it.customColors.copy(enabled = enabled)) }
                }
            )

            Text(
                text = "QUICK PALETTES",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPalettes.forEach { pal ->
                    StudioOptionChip(
                        label = pal.name,
                        selected = draft.customColors.enabled && draft.customColors.primaryArgb == pal.primary,
                        onClick = {
                            onUpdateDraft {
                                it.copy(
                                    customColors = CustomColorOverrides(
                                        enabled = true,
                                        primaryArgb = pal.primary,
                                        accentArgb = pal.accent,
                                        backgroundArgb = pal.bg,
                                        surfaceArgb = pal.surface,
                                        primaryTextArgb = pal.text,
                                        borderArgb = pal.border
                                    )
                                )
                            }
                        }
                    )
                }
            }

            Text(
                text = "EDIT TARGET COLOR SLOT",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.secondaryText
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorSlot.entries.forEach { slot ->
                    StudioOptionChip(
                        label = slot.label,
                        selected = selectedSlot == slot,
                        onClick = { selectedSlot = slot }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedSlot.label.uppercase()} HEX",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.secondaryText
                )
                Text(
                    text = "#%06X".format((activeSlotArgb and 0xFFFFFFL)),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tokens.primaryText
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorSwatches.forEach { swatch ->
                    ColorSwatchCircle(
                        colorArgb = swatch,
                        selected = activeSlotArgb == swatch,
                        onClick = { applySlotArgb(swatch) }
                    )
                }
            }

            val currentColor = Color(activeSlotArgb)
            val redVal = (currentColor.red * 255f)
            val greenVal = (currentColor.green * 255f)
            val blueVal = (currentColor.blue * 255f)

            StudioSliderRow(
                label = "Red",
                valueText = "${redVal.toInt()}",
                value = redVal,
                valueRange = 0f..255f,
                onValueChange = { r ->
                    val next = android.graphics.Color.rgb(r.toInt(), greenVal.toInt(), blueVal.toInt())
                    applySlotArgb(next.toLong() and 0xFFFFFFFFL)
                }
            )
            StudioSliderRow(
                label = "Green",
                valueText = "${greenVal.toInt()}",
                value = greenVal,
                valueRange = 0f..255f,
                onValueChange = { g ->
                    val next = android.graphics.Color.rgb(redVal.toInt(), g.toInt(), blueVal.toInt())
                    applySlotArgb(next.toLong() and 0xFFFFFFFFL)
                }
            )
            StudioSliderRow(
                label = "Blue",
                valueText = "${blueVal.toInt()}",
                value = blueVal,
                valueRange = 0f..255f,
                onValueChange = { b ->
                    val next = android.graphics.Color.rgb(redVal.toInt(), greenVal.toInt(), b.toInt())
                    applySlotArgb(next.toLong() and 0xFFFFFFFFL)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.primary,
                        contentColor = tokens.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_custom_theme_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE AS THEME", maxLines = 1)
                }

                OutlinedButton(
                    onClick = {
                        onUpdateDraft {
                            it.copy(
                                themeId = ThemeRegistry.blueprintBlue.id,
                                customColors = CustomColorOverrides(enabled = false)
                            )
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.5.dp, tokens.border),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset colors", tint = tokens.primary)
                }
            }
        }

        if (customThemes.isNotEmpty()) {
            StudioSectionCard(
                title = "Manage Custom Themes",
                subtitle = "Duplicate, rename, or remove your saved themes."
            ) {
                customThemes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(tokens.background)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(theme.primaryArgb))
                            )
                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.primaryText
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    onUpdateDraft { it.copy(themeId = theme.id) }
                                    onSaveCustomTheme("${theme.name} Copy", systemDark)
                                }
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate", tint = tokens.primary)
                            }
                            IconButton(onClick = { renameTarget = theme }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = tokens.primary)
                            }
                            IconButton(onClick = { onDeleteCustomTheme(theme.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = tokens.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("My Palette") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = tokens.background,
            title = { Text("Save Custom Theme", color = tokens.primaryText) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Theme Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCreateDialog = false
                        onSaveCustomTheme(nameInput, systemDark)
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    renameTarget?.let { target ->
        var newName by remember(target.id) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = tokens.background,
            title = { Text("Rename Theme", color = tokens.primaryText) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Theme Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        renameTarget = null
                        onRenameCustomTheme(target.id, newName)
                    }
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }
}
