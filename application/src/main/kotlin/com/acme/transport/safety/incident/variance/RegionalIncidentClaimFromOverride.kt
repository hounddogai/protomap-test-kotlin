package com.acme.transport.safety.incident.variance

import com.acme.transport.safety.cameraevent.group.CameraEventOverride
import com.acme.transport.safety.cameraevent.group.seasonalCameraEventEntryWeight

fun summarizeCameraEventOverridesForClaim(items: MutableList<CameraEventOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRegionalIncidentClaimsFromOverride(items: MutableList<CameraEventOverride>): MutableList<RegionalIncidentClaim> {
    val out = mutableListOf<RegionalIncidentClaim>()
    for (item in items) {
        val derived = newRegionalIncidentClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignRegionalIncidentClaim(item: RegionalIncidentClaim): Double = seasonalCameraEventEntryWeight(7, true)
