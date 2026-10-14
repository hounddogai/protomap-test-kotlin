package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.utilization.window.UtilizationWindow
import com.acme.transport.fleetops.utilization.window.normalizeUtilizationWindowCode

fun summarizeUtilizationWindowsForMapping(items: MutableList<UtilizationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveUtilizationMappingsFromWindow(items: MutableList<UtilizationWindow>): MutableList<UtilizationMapping> {
    val out = mutableListOf<UtilizationMapping>()
    for (item in items) {
        val derived = newUtilizationMapping(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyUtilizationMapping(item: UtilizationMapping): String = normalizeUtilizationWindowCode(item.code)
