package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.declaration.estimate.DeclarationEstimate
import com.acme.transport.customs.declaration.estimate.weightedDeclarationClaimBucket

fun summarizeDeclarationEstimatesForEntry(items: MutableList<DeclarationEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingHoldEntriesFromEstimate(items: MutableList<DeclarationEstimate>): MutableList<PendingHoldEntry> {
    val out = mutableListOf<PendingHoldEntry>()
    for (item in items) {
        val derived = newPendingHoldEntry(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePendingHoldEntry(item: PendingHoldEntry): String {
    return weightedDeclarationClaimBucket(item.adjustedTotal())
}
