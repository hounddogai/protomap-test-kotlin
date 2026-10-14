package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.customs.inspection.metric.DefaultInspectionEstimate
import com.acme.transport.customs.inspection.metric.classifyDefaultInspectionEstimate

fun summarizeDefaultInspectionEstimatesForVariance(items: MutableList<DefaultInspectionEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackClearanceVariancesFromEstimate(items: MutableList<DefaultInspectionEstimate>): MutableList<FallbackClearanceVariance> {
    val out = mutableListOf<FallbackClearanceVariance>()
    for (item in items) {
        val derived = newFallbackClearanceVariance(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeFallbackClearanceVariance(item: FallbackClearanceVariance): String {
    return classifyDefaultInspectionEstimate(item.measuredValue())
}
