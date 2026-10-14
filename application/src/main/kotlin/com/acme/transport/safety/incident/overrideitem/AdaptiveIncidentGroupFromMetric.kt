package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.safety.incident.preference.LegacyIncidentMetric
import com.acme.transport.safety.incident.preference.incidentAuditWeight

fun summarizeLegacyIncidentMetricsForGroup(items: MutableList<LegacyIncidentMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveIncidentGroupsFromMetric(items: MutableList<LegacyIncidentMetric>): MutableList<AdaptiveIncidentGroup> {
    val out = mutableListOf<AdaptiveIncidentGroup>()
    for (item in items) {
        val derived = newAdaptiveIncidentGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreAdaptiveIncidentGroup(item: AdaptiveIncidentGroup): Double = incidentAuditWeight(6, true)
