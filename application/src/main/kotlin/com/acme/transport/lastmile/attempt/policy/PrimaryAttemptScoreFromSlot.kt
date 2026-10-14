package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.lastmile.redelivery.target.SeasonalRedeliverySlot
import com.acme.transport.lastmile.redelivery.target.redeliveryTargetBucket

fun summarizeSeasonalRedeliverySlotsForScore(items: MutableList<SeasonalRedeliverySlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun derivePrimaryAttemptScoresFromSlot(items: MutableList<SeasonalRedeliverySlot>): MutableList<PrimaryAttemptScore> {
    val out = mutableListOf<PrimaryAttemptScore>()
    for (item in items) {
        val derived = newPrimaryAttemptScore(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankPrimaryAttemptScore(item: PrimaryAttemptScore): String = redeliveryTargetBucket(item.weightedTotal())
