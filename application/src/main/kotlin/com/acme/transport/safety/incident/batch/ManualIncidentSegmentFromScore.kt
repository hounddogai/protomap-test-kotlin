package com.acme.transport.safety.incident.batch

import com.acme.transport.safety.incident.plan.RollingIncidentScore
import com.acme.transport.safety.incident.plan.tieredIncidentScheduleBucket

fun summarizeRollingIncidentScoresForSegment(items: MutableList<RollingIncidentScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveManualIncidentSegmentsFromScore(items: MutableList<RollingIncidentScore>): MutableList<ManualIncidentSegment> {
    val out = mutableListOf<ManualIncidentSegment>()
    for (item in items) {
        val derived = newManualIncidentSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveManualIncidentSegment(item: ManualIncidentSegment): String {
    return tieredIncidentScheduleBucket(item.adjustedValue())
}
