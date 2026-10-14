package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.safety.cameraevent.rollup.StrictCameraEventMetric
import com.acme.transport.safety.cameraevent.rollup.classifyCameraEventManifest

fun summarizeStrictCameraEventMetricsForDraft(items: MutableList<StrictCameraEventMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictIncidentDraftsFromMetric(items: MutableList<StrictCameraEventMetric>): MutableList<StrictIncidentDraft> {
    val out = mutableListOf<StrictIncidentDraft>()
    for (item in items) {
        val derived = newStrictIncidentDraft(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveStrictIncidentDraft(item: StrictIncidentDraft): String {
    return classifyCameraEventManifest(item.adjustedLoad())
}
