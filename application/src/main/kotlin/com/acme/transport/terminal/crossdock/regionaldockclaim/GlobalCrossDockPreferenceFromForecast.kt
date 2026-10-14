package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.terminal.dockdoor.doorrecord.RollingDockDoorForecast
import com.acme.transport.terminal.dockdoor.doorrecord.weightedDockDoorAuditWeight

fun summarizeRollingDockDoorForecastsForPreference(items: MutableList<RollingDockDoorForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveGlobalCrossDockPreferencesFromForecast(items: MutableList<RollingDockDoorForecast>): MutableList<GlobalCrossDockPreference> {
    val out = mutableListOf<GlobalCrossDockPreference>()
    for (item in items) {
        val derived = newGlobalCrossDockPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateGlobalCrossDockPreference(item: GlobalCrossDockPreference): Double {
    return weightedDockDoorAuditWeight(2, true)
}
