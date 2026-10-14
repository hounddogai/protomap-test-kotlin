package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.customs.clearance.threshold.ClearanceThreshold
import com.acme.transport.customs.clearance.threshold.clearanceThresholdBucket

fun summarizeClearanceThresholdsForPlan(items: MutableList<ClearanceThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientInspectionPlansFromThreshold(items: MutableList<ClearanceThreshold>): MutableList<LenientInspectionPlan> {
    val out = mutableListOf<LenientInspectionPlan>()
    for (item in items) {
        val derived = newLenientInspectionPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLenientInspectionPlan(item: LenientInspectionPlan): String {
    return clearanceThresholdBucket(item.effectiveTotal())
}
