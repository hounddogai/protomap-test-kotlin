package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.safety.incident.incidentsnapshot.IncidentForecast
import com.acme.transport.safety.incident.incidentsnapshot.normalizeWeightedIncidentSnapshotCode

fun summarizeIncidentForecastsForMilestone(items: MutableList<IncidentForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePendingNearMissMilestonesFromForecast(items: MutableList<IncidentForecast>): MutableList<PendingNearMissMilestone> {
    val out = mutableListOf<PendingNearMissMilestone>()
    for (item in items) {
        val derived = newPendingNearMissMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchPendingNearMissMilestone(item: PendingNearMissMilestone): String {
    return normalizeWeightedIncidentSnapshotCode(item.code)
}
