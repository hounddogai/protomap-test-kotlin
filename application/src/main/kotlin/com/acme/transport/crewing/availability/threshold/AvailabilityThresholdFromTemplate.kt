package com.acme.transport.crewing.availability.threshold

import com.acme.transport.crewing.swap.bundle.CappedSwapTemplate
import com.acme.transport.crewing.swap.bundle.classifyBatchedSwapBundle

fun summarizeCappedSwapTemplatesForThreshold(items: MutableList<CappedSwapTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveAvailabilityThresholdsFromTemplate(items: MutableList<CappedSwapTemplate>): MutableList<AvailabilityThreshold> {
    val out = mutableListOf<AvailabilityThreshold>()
    for (item in items) {
        val derived = newAvailabilityThreshold(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitAvailabilityThreshold(item: AvailabilityThreshold): String {
    return classifyBatchedSwapBundle(item.adjustedTotal())
}
