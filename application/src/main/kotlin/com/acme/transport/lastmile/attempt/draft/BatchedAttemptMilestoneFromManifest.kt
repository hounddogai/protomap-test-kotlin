package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.lastmile.redelivery.checkpoint.RedeliveryManifest
import com.acme.transport.lastmile.redelivery.checkpoint.normalizeRedeliveryManifestCode

fun summarizeRedeliveryManifestsForMilestone(items: MutableList<RedeliveryManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedAttemptMilestonesFromManifest(items: MutableList<RedeliveryManifest>): MutableList<BatchedAttemptMilestone> {
    val out = mutableListOf<BatchedAttemptMilestone>()
    for (item in items) {
        val derived = newBatchedAttemptMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planBatchedAttemptMilestone(item: BatchedAttemptMilestone): String {
    return normalizeRedeliveryManifestCode(item.code)
}
