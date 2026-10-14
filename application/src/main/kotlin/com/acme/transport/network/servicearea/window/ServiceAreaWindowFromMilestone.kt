package com.acme.transport.network.servicearea.window

import com.acme.transport.network.servicearea.milestone.LenientServiceAreaMilestone
import com.acme.transport.network.servicearea.milestone.classifySeasonalServiceAreaLedger

fun summarizeLenientServiceAreaMilestonesForWindow(items: MutableList<LenientServiceAreaMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveServiceAreaWindowsFromMilestone(items: MutableList<LenientServiceAreaMilestone>): MutableList<ServiceAreaWindow> {
    val out = mutableListOf<ServiceAreaWindow>()
    for (item in items) {
        val derived = newServiceAreaWindow(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeServiceAreaWindow(item: ServiceAreaWindow): String {
    return classifySeasonalServiceAreaLedger(item.measuredValue())
}
