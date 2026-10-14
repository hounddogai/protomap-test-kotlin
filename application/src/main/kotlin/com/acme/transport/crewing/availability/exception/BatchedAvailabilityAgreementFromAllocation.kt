package com.acme.transport.crewing.availability.exception

import com.acme.transport.crewing.availability.claim.LenientAvailabilityAllocation
import com.acme.transport.crewing.availability.claim.classifyAvailabilityMetric

fun summarizeLenientAvailabilityAllocationsForAgreement(items: MutableList<LenientAvailabilityAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBatchedAvailabilityAgreementsFromAllocation(items: MutableList<LenientAvailabilityAllocation>): MutableList<BatchedAvailabilityAgreement> {
    val out = mutableListOf<BatchedAvailabilityAgreement>()
    for (item in items) {
        val derived = newBatchedAvailabilityAgreement(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveBatchedAvailabilityAgreement(item: BatchedAvailabilityAgreement): String {
    return classifyAvailabilityMetric(item.weightedTotal())
}
