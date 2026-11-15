package com.example.personalization.layouts

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.personalization.model.DashboardWidgetConfig
import com.example.personalization.model.DashboardWidgetId
import com.example.personalization.model.DensityMode
import com.example.personalization.model.WidgetSize

object LayoutManager {

    fun scaleDp(baseDp: Dp, density: DensityMode): Dp {
        return (baseDp.value * density.spacingMultiplier).dp
    }

    fun moveWidget(
        widgets: List<DashboardWidgetConfig>,
        fromIndex: Int,
        toIndex: Int
    ): List<DashboardWidgetConfig> {
        if (fromIndex !in widgets.indices || toIndex !in widgets.indices || fromIndex == toIndex) {
            return widgets
        }
        val mutable = widgets.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        return mutable
    }

    fun toggleWidgetVisibility(
        widgets: List<DashboardWidgetConfig>,
        id: DashboardWidgetId
    ): List<DashboardWidgetConfig> {
        return widgets.map {
            if (it.id == id) it.copy(visible = !it.visible) else it
        }
    }

    fun toggleWidgetSize(
        widgets: List<DashboardWidgetConfig>,
        id: DashboardWidgetId
    ): List<DashboardWidgetConfig> {
        return widgets.map {
            if (it.id == id) {
                val nextSize = if (it.size == WidgetSize.SMALL) WidgetSize.WIDE else WidgetSize.SMALL
                it.copy(size = nextSize)
            } else {
                it
            }
        }
    }
}
