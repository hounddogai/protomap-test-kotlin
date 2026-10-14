package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.safety.violation.metric.ManualViolationMetric
import com.acme.transport.safety.violation.metric.classifyManualViolationMetric

fun summarizeManualViolationMetricsForAdjustment(items: MutableList<ManualViolationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveLenientIncidentAdjustmentsFromMetric(items: MutableList<ManualViolationMetric>): MutableList<LenientIncidentAdjustment> {
    val out = mutableListOf<LenientIncidentAdjustment>()
    for (item in items) {
        val derived = newLenientIncidentAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignLenientIncidentAdjustment(item: LenientIncidentAdjustment): String {
    return classifyManualViolationMetric(item.weightedValue())
}
