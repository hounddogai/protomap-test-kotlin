package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.lastmile.redelivery.threshold.TieredRedeliveryCheckpoint
import com.acme.transport.lastmile.redelivery.threshold.normalizeRedeliveryNoticeCode

fun summarizeTieredRedeliveryCheckpointsForTier(items: MutableList<TieredRedeliveryCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAttemptTiersFromCheckpoint(items: MutableList<TieredRedeliveryCheckpoint>): MutableList<AttemptTier> {
    val out = mutableListOf<AttemptTier>()
    for (item in items) {
        val derived = newAttemptTier(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveAttemptTier(item: AttemptTier): String = normalizeRedeliveryNoticeCode(item.code)
