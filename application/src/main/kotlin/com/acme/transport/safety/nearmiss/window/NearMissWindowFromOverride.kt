package com.acme.transport.safety.nearmiss.window

import com.acme.transport.safety.incident.baseline.IncidentOverride
import com.acme.transport.safety.incident.baseline.classifyIncidentOverride

fun summarizeIncidentOverridesForWindow(items: MutableList<IncidentOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveNearMissWindowsFromOverride(items: MutableList<IncidentOverride>): MutableList<NearMissWindow> {
    val out = mutableListOf<NearMissWindow>()
    for (item in items) {
        val derived = newNearMissWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchNearMissWindow(item: NearMissWindow): String = classifyIncidentOverride(item.weightedTotal())
