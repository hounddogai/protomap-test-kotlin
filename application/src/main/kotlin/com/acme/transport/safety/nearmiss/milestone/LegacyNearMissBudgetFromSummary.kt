package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.safety.cameraevent.summary.CameraEventSummary
import com.acme.transport.safety.cameraevent.summary.classifyCameraEventProfile

fun summarizeCameraEventSummariesForBudget(items: MutableList<CameraEventSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveLegacyNearMissBudgetsFromSummary(items: MutableList<CameraEventSummary>): MutableList<LegacyNearMissBudget> {
    val out = mutableListOf<LegacyNearMissBudget>()
    for (item in items) {
        val derived = newLegacyNearMissBudget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreLegacyNearMissBudget(item: LegacyNearMissBudget): String {
    return classifyCameraEventProfile(item.measuredValue())
}
