package com.acme.transport.network.zone.zonequota

import com.acme.transport.network.zone.claim.FallbackZoneThreshold
import com.acme.transport.network.zone.claim.classifyCappedZoneClaim

fun summarizeFallbackZoneThresholdsForWindow(items: MutableList<FallbackZoneThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRollingZoneWindowsFromThreshold(items: MutableList<FallbackZoneThreshold>): MutableList<RollingZoneWindow> {
    val out = mutableListOf<RollingZoneWindow>()
    for (item in items) {
        val derived = newRollingZoneWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyRollingZoneWindow(item: RollingZoneWindow): String = classifyCappedZoneClaim(item.projectedValue())
