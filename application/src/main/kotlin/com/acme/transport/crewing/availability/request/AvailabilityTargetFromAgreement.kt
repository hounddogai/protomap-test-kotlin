package com.acme.transport.crewing.availability.request

import com.acme.transport.crewing.availability.exception.BatchedAvailabilityAgreement
import com.acme.transport.crewing.availability.exception.batchedAvailabilityAgreementBucket

fun summarizeBatchedAvailabilityAgreementsForTarget(items: MutableList<BatchedAvailabilityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAvailabilityTargetsFromAgreement(items: MutableList<BatchedAvailabilityAgreement>): MutableList<AvailabilityTarget> {
    val out = mutableListOf<AvailabilityTarget>()
    for (item in items) {
        val derived = newAvailabilityTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankAvailabilityTarget(item: AvailabilityTarget): String {
    return batchedAvailabilityAgreementBucket(item.adjustedTotal())
}
