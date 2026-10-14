package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.clearance.reservation.ClearanceSummary
import com.acme.transport.customs.clearance.reservation.defaultClearanceBaselineWeight

fun summarizeClearanceSummariesForQuota(items: MutableList<ClearanceSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRollingBrokerQuotasFromSummary(items: MutableList<ClearanceSummary>): MutableList<RollingBrokerQuota> {
    val out = mutableListOf<RollingBrokerQuota>()
    for (item in items) {
        val derived = newRollingBrokerQuota(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRollingBrokerQuota(item: RollingBrokerQuota): Double = defaultClearanceBaselineWeight(8, true)
