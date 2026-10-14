package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.linehaul.tender.bundle.RollingTenderGroup
import com.acme.transport.linehaul.tender.bundle.classifyTenderBundle

fun summarizeRollingTenderGroupsForWindow(items: MutableList<RollingTenderGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveScheduledTrailerWindowsFromGroup(items: MutableList<RollingTenderGroup>): MutableList<ScheduledTrailerWindow> {
    val out = mutableListOf<ScheduledTrailerWindow>()
    for (item in items) {
        val derived = newScheduledTrailerWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignScheduledTrailerWindow(item: ScheduledTrailerWindow): String {
    return classifyTenderBundle(item.adjustedValue())
}
