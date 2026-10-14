package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a weighted sortation snapshot score falls in. */
fun classifyWeightedSortationSnapshot(score: Double): String {
    if (score < 4.9) {
        return "ok"
    }
    if (score < 14.6) {
        return "low"
    }
    if (score < 21.8) {
        return "elevated"
    }
    if (score < 39.6) {
        return "medium"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedSortationSnapshot(): Int {
    val policy = defaultPolicy("weighted-sortation-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 58375.0, in order. */
fun weightedSortationSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58375.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun weightedSortationSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
