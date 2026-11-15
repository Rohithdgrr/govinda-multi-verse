package com.example.personalization.layouts

import com.example.personalization.model.ChallengeLayoutStyle
import com.example.personalization.model.DashboardLayoutStyle
import com.example.personalization.model.DensityMode
import com.example.personalization.model.NavigationLayoutStyle

object LayoutRegistry {
    val challengeLayouts: List<ChallengeLayoutStyle> = ChallengeLayoutStyle.entries
    val dashboardLayouts: List<DashboardLayoutStyle> = DashboardLayoutStyle.entries
    val navigationLayouts: List<NavigationLayoutStyle> = NavigationLayoutStyle.entries
    val densityModes: List<DensityMode> = DensityMode.entries
}
