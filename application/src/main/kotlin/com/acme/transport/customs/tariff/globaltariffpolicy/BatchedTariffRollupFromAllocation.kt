package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.clearance.quota.PendingClearanceAllocation
import com.acme.transport.customs.clearance.quota.classifyScheduledClearanceVariance

fun summarizePendingClearanceAllocationsForRollup(items: MutableList<PendingClearanceAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveBatchedTariffRollupsFromAllocation(items: MutableList<PendingClearanceAllocation>): MutableList<BatchedTariffRollup> {
    val out = mutableListOf<BatchedTariffRollup>()
    for (item in items) {
        val derived = newBatchedTariffRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreBatchedTariffRollup(item: BatchedTariffRollup): String {
    return classifyScheduledClearanceVariance(item.projectedTotal())
}
