package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.inspection.estimate.LenientInspectionEstimate
import com.acme.transport.customs.inspection.estimate.normalizeLenientInspectionEstimateCode

fun summarizeLenientInspectionEstimatesForAssignment(items: MutableList<LenientInspectionEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveAdaptiveBrokerAssignmentsFromEstimate(items: MutableList<LenientInspectionEstimate>): MutableList<AdaptiveBrokerAssignment> {
    val out = mutableListOf<AdaptiveBrokerAssignment>()
    for (item in items) {
        val derived = newAdaptiveBrokerAssignment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateAdaptiveBrokerAssignment(item: AdaptiveBrokerAssignment): String {
    return normalizeLenientInspectionEstimateCode(item.code)
}
