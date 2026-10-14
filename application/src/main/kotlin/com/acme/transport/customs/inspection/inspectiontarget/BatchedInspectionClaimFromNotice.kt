package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.customs.broker.entry.BrokerNotice
import com.acme.transport.customs.broker.entry.classifyPrimaryBrokerEntry

fun summarizeBrokerNoticesForClaim(items: MutableList<BrokerNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveBatchedInspectionClaimsFromNotice(items: MutableList<BrokerNotice>): MutableList<BatchedInspectionClaim> {
    val out = mutableListOf<BatchedInspectionClaim>()
    for (item in items) {
        val derived = newBatchedInspectionClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planBatchedInspectionClaim(item: BatchedInspectionClaim): String {
    return classifyPrimaryBrokerEntry(item.adjustedExposure())
}
