package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.safety.incident.metric.DefaultIncidentCycle
import com.acme.transport.safety.incident.metric.normalizeDefaultIncidentCycleCode

fun summarizeDefaultIncidentCyclesForManifest(items: MutableList<DefaultIncidentCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLegacyIncidentManifestsFromCycle(items: MutableList<DefaultIncidentCycle>): MutableList<LegacyIncidentManifest> {
    val out = mutableListOf<LegacyIncidentManifest>()
    for (item in items) {
        val derived = newLegacyIncidentManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacyIncidentManifest(item: LegacyIncidentManifest): String {
    return normalizeDefaultIncidentCycleCode(item.code)
}
