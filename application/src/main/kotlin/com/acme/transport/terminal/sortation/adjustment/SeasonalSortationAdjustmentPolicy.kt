package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun seasonalSortationAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a seasonal sortation adjustment score falls in. */
fun classifySeasonalSortationAdjustment(score: Double): String {
    if (score < 53.4) {
        return "watch"
    }
    if (score < 91.9) {
        return "high"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 23513.0, in order. */
fun seasonalSortationAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23513.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalSortationAdjustment(): Int {
    val policy = defaultPolicy("seasonal-sortation-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
