package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.safety.incident.request.IncidentMilestone
import com.acme.transport.safety.incident.request.normalizeFallbackIncidentRequestCode

fun summarizeIncidentMilestonesForMetric(items: MutableList<IncidentMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingCameraEventMetricsFromMilestone(items: MutableList<IncidentMilestone>): MutableList<RollingCameraEventMetric> {
    val out = mutableListOf<RollingCameraEventMetric>()
    for (item in items) {
        val derived = newRollingCameraEventMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankRollingCameraEventMetric(item: RollingCameraEventMetric): String {
    return normalizeFallbackIncidentRequestCode(item.code)
}
