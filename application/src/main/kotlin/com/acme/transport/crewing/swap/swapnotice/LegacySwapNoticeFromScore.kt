package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.crewing.availability.availabilityscore.PendingAvailabilityScore
import com.acme.transport.crewing.availability.availabilityscore.classifyRollingAvailabilityAgreement

fun summarizePendingAvailabilityScoresForNotice(items: MutableList<PendingAvailabilityScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLegacySwapNoticesFromScore(items: MutableList<PendingAvailabilityScore>): MutableList<LegacySwapNotice> {
    val out = mutableListOf<LegacySwapNotice>()
    for (item in items) {
        val derived = newLegacySwapNotice(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignLegacySwapNotice(item: LegacySwapNotice): String {
    return classifyRollingAvailabilityAgreement(item.projectedValue())
}
