package com.acme.transport.crewing.swap.metric

import com.acme.transport.crewing.availability.profile.GlobalAvailabilityAllocation
import com.acme.transport.crewing.availability.profile.pendingAvailabilityPreferenceWeight

fun summarizeGlobalAvailabilityAllocationsForMetric(items: MutableList<GlobalAvailabilityAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveManualSwapMetricsFromAllocation(items: MutableList<GlobalAvailabilityAllocation>): MutableList<ManualSwapMetric> {
    val out = mutableListOf<ManualSwapMetric>()
    for (item in items) {
        val derived = newManualSwapMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectManualSwapMetric(item: ManualSwapMetric): Double = pendingAvailabilityPreferenceWeight(2, true)
