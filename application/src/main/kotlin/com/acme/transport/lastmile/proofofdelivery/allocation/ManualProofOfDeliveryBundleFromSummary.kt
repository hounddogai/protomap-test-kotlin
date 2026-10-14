package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.lastmile.attempt.attemptmetric.AttemptSummary
import com.acme.transport.lastmile.attempt.attemptmetric.normalizeRegionalAttemptMetricCode

fun summarizeAttemptSummariesForBundle(items: MutableList<AttemptSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveManualProofOfDeliveryBundlesFromSummary(items: MutableList<AttemptSummary>): MutableList<ManualProofOfDeliveryBundle> {
    val out = mutableListOf<ManualProofOfDeliveryBundle>()
    for (item in items) {
        val derived = newManualProofOfDeliveryBundle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileManualProofOfDeliveryBundle(item: ManualProofOfDeliveryBundle): String {
    return normalizeRegionalAttemptMetricCode(item.code)
}
