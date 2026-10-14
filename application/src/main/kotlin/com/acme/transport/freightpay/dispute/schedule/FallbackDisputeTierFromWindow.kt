package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.audit.digest.WeightedAuditWindow
import com.acme.transport.freightpay.audit.digest.classifyWeightedAuditWindow

fun summarizeWeightedAuditWindowsForTier(items: MutableList<WeightedAuditWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFallbackDisputeTiersFromWindow(items: MutableList<WeightedAuditWindow>): MutableList<FallbackDisputeTier> {
    val out = mutableListOf<FallbackDisputeTier>()
    for (item in items) {
        val derived = newFallbackDisputeTier(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveFallbackDisputeTier(item: FallbackDisputeTier): String {
    return classifyWeightedAuditWindow(item.measuredValue())
}
