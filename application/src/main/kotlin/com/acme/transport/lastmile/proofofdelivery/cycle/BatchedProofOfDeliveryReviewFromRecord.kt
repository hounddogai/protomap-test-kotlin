package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.lastmile.proofofdelivery.deliveryclaim.ManualProofOfDeliveryRecord
import com.acme.transport.lastmile.proofofdelivery.deliveryclaim.manualProofOfDeliveryClaimBucket

fun summarizeManualProofOfDeliveryRecordsForReview(items: MutableList<ManualProofOfDeliveryRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedProofOfDeliveryReviewsFromRecord(items: MutableList<ManualProofOfDeliveryRecord>): MutableList<BatchedProofOfDeliveryReview> {
    val out = mutableListOf<BatchedProofOfDeliveryReview>()
    for (item in items) {
        val derived = newBatchedProofOfDeliveryReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun mergeBatchedProofOfDeliveryReview(item: BatchedProofOfDeliveryReview): String {
    return manualProofOfDeliveryClaimBucket(item.adjustedLoad())
}
