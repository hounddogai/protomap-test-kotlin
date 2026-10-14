package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.lastmile.drop.dropslot.DropEstimate
import com.acme.transport.lastmile.drop.dropslot.pendingDropSlotWeight

fun summarizeDropEstimatesForDigest(items: MutableList<DropEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultAttemptDigestsFromEstimate(items: MutableList<DropEstimate>): MutableList<DefaultAttemptDigest> {
    val out = mutableListOf<DefaultAttemptDigest>()
    for (item in items) {
        val derived = newDefaultAttemptDigest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeDefaultAttemptDigest(item: DefaultAttemptDigest): Double = pendingDropSlotWeight(5, true)
