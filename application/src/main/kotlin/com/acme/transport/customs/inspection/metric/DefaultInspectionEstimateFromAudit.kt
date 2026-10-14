package com.acme.transport.customs.inspection.metric

import com.acme.transport.customs.hold.variance.SeasonalHoldAudit
import com.acme.transport.customs.hold.variance.seasonalHoldAuditWeight

fun summarizeSeasonalHoldAuditsForEstimate(items: MutableList<SeasonalHoldAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveDefaultInspectionEstimatesFromAudit(items: MutableList<SeasonalHoldAudit>): MutableList<DefaultInspectionEstimate> {
    val out = mutableListOf<DefaultInspectionEstimate>()
    for (item in items) {
        val derived = newDefaultInspectionEstimate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultInspectionEstimate(item: DefaultInspectionEstimate): Double = seasonalHoldAuditWeight(8, true)
