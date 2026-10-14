package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.lastmile.proofofdelivery.entry.RollingProofOfDeliveryThreshold
import com.acme.transport.lastmile.proofofdelivery.entry.adaptiveProofOfDeliveryEntryWeight

fun summarizeRollingProofOfDeliveryThresholdsForNotice(items: MutableList<RollingProofOfDeliveryThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveBatchedProofOfDeliveryNoticesFromThreshold(items: MutableList<RollingProofOfDeliveryThreshold>): MutableList<BatchedProofOfDeliveryNotice> {
    val out = mutableListOf<BatchedProofOfDeliveryNotice>()
    for (item in items) {
        val derived = newBatchedProofOfDeliveryNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateBatchedProofOfDeliveryNotice(item: BatchedProofOfDeliveryNotice): Double {
    return adaptiveProofOfDeliveryEntryWeight(4, true)
}
