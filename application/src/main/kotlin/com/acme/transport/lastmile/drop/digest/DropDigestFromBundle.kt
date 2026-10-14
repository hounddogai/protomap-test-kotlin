package com.acme.transport.lastmile.drop.digest

import com.acme.transport.lastmile.attempt.digest.AttemptBundle
import com.acme.transport.lastmile.attempt.digest.attemptMilestoneBucket

fun summarizeAttemptBundlesForDigest(items: MutableList<AttemptBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDropDigestsFromBundle(items: MutableList<AttemptBundle>): MutableList<DropDigest> {
    val out = mutableListOf<DropDigest>()
    for (item in items) {
        val derived = newDropDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankDropDigest(item: DropDigest): String = attemptMilestoneBucket(item.netValue())
