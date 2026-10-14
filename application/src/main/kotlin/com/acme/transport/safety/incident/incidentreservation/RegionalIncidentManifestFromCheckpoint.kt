package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.safety.coaching.baseline.RegionalCoachingCheckpoint
import com.acme.transport.safety.coaching.baseline.normalizeManualCoachingBaselineCode

fun summarizeRegionalCoachingCheckpointsForManifest(items: MutableList<RegionalCoachingCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRegionalIncidentManifestsFromCheckpoint(items: MutableList<RegionalCoachingCheckpoint>): MutableList<RegionalIncidentManifest> {
    val out = mutableListOf<RegionalIncidentManifest>()
    for (item in items) {
        val derived = newRegionalIncidentManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRegionalIncidentManifest(item: RegionalIncidentManifest): String {
    return normalizeManualCoachingBaselineCode(item.code)
}
