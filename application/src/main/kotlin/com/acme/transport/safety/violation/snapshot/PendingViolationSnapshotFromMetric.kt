package com.acme.transport.safety.violation.snapshot

import com.acme.transport.safety.incident.metric.IncidentMetric
import com.acme.transport.safety.incident.metric.normalizeDefaultIncidentCycleCode

fun summarizeIncidentMetricsForSnapshot(items: MutableList<IncidentMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingViolationSnapshotsFromMetric(items: MutableList<IncidentMetric>): MutableList<PendingViolationSnapshot> {
    val out = mutableListOf<PendingViolationSnapshot>()
    for (item in items) {
        val derived = newPendingViolationSnapshot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankPendingViolationSnapshot(item: PendingViolationSnapshot): String {
    return normalizeDefaultIncidentCycleCode(item.code)
}
