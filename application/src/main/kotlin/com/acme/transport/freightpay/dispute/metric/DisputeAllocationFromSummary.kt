package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.fuelsurcharge.milestone.FuelSurchargeSummary
import com.acme.transport.freightpay.fuelsurcharge.milestone.normalizeFuelSurchargeSummaryCode

fun summarizeFuelSurchargeSummariesForAllocation(items: MutableList<FuelSurchargeSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDisputeAllocationsFromSummary(items: MutableList<FuelSurchargeSummary>): MutableList<DisputeAllocation> {
    val out = mutableListOf<DisputeAllocation>()
    for (item in items) {
        val derived = newDisputeAllocation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeDisputeAllocation(item: DisputeAllocation): String = normalizeFuelSurchargeSummaryCode(item.code)
