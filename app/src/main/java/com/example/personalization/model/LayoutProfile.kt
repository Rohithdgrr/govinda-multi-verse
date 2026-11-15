package com.example.personalization.model

import androidx.compose.runtime.Immutable

enum class ChallengeLayoutStyle(val displayName: String, val description: String) {
    CENTERED_EQUATION("Centered Equation", "Balanced equation card above docked keypad"),
    LARGE_EQUATION("Large Equation", "Oversized high-legibility expression display"),
    COMPACT_EQUATION("Compact Equation", "Tighter vertical footprint for smaller screens"),
    SPLIT_PANEL("Split Panel", "Side-by-side layout on wide screens & stacked on compact"),
    STATS_ASSISTED("Stats-Assisted", "Includes customizable mini telemetry widgets strip"),
    FULLSCREEN_FOCUS("Fullscreen Focus", "Distraction-free surface with minimal chrome")
}

enum class DashboardLayoutStyle(val displayName: String) {
    COMPACT("Compact"),
    STANDARD("Standard"),
    STATISTICS_FIRST("Statistics-First"),
    CHALLENGE_FIRST("Challenge-First"),
    CARD_BASED("Card-Based"),
    MINIMAL("Minimal"),
    DEVELOPER_CONSOLE("Developer Console"),
    MODULAR_GRID("Modular Grid")
}

enum class NavigationLayoutStyle(val displayName: String) {
    STANDARD_TOP_BAR("Standard Top Bar"),
    COMPACT_TOP_BAR("Compact Top Bar"),
    ICON_ONLY("Icon-Only Bar"),
    PILL_HEADER("Pill Header Bar")
}

enum class DensityMode(val displayName: String, val spacingMultiplier: Float) {
    COMPACT("Compact", 0.78f),
    COMFORTABLE("Comfortable", 1.0f),
    SPACIOUS("Spacious", 1.24f)
}

enum class DashboardWidgetId(val displayName: String) {
    DAILY_STREAK("Daily Streak"),
    XP("XP Counter"),
    SKILL_PROFILE("Skill Profile"),
    DAILY_TARGET("Daily Target"),
    RECENT_ACTIVITY("Recent Activity"),
    WEAK_TOPICS("Weak Topics"),
    CURRENT_DIFFICULTY("Current Difficulty"),
    ACCURACY_CHART("Accuracy Chart"),
    PRACTICE_HISTORY("Practice History")
}

enum class WidgetSize(val displayName: String) {
    SMALL("1x1"),
    WIDE("2x1")
}

@Immutable
data class DashboardWidgetConfig(
    val id: DashboardWidgetId,
    val visible: Boolean = true,
    val size: WidgetSize = WidgetSize.SMALL
)

@Immutable
data class LayoutProfile(
    val challengeLayout: ChallengeLayoutStyle = ChallengeLayoutStyle.CENTERED_EQUATION,
    val dashboardLayout: DashboardLayoutStyle = DashboardLayoutStyle.MINIMAL,
    val navigationLayout: NavigationLayoutStyle = NavigationLayoutStyle.STANDARD_TOP_BAR,
    val density: DensityMode = DensityMode.COMFORTABLE,
    val widgets: List<DashboardWidgetConfig> = listOf(
        DashboardWidgetConfig(DashboardWidgetId.DAILY_STREAK, visible = true, size = WidgetSize.SMALL),
        DashboardWidgetConfig(DashboardWidgetId.CURRENT_DIFFICULTY, visible = true, size = WidgetSize.SMALL),
        DashboardWidgetConfig(DashboardWidgetId.ACCURACY_CHART, visible = true, size = WidgetSize.WIDE),
        DashboardWidgetConfig(DashboardWidgetId.DAILY_TARGET, visible = false, size = WidgetSize.SMALL),
        DashboardWidgetConfig(DashboardWidgetId.SKILL_PROFILE, visible = false, size = WidgetSize.WIDE),
        DashboardWidgetConfig(DashboardWidgetId.WEAK_TOPICS, visible = false, size = WidgetSize.SMALL),
        DashboardWidgetConfig(DashboardWidgetId.RECENT_ACTIVITY, visible = false, size = WidgetSize.WIDE),
        DashboardWidgetConfig(DashboardWidgetId.PRACTICE_HISTORY, visible = false, size = WidgetSize.WIDE),
        DashboardWidgetConfig(DashboardWidgetId.XP, visible = false, size = WidgetSize.SMALL)
    )
)
