package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun batchedZoneScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a batched zone schedule score falls in. */
fun classifyBatchedZoneSchedule(score: Double): String {
    if (score < 54.4) {
        return "low"
    }
    if (score < 95.2) {
        return "elevated"
    }
    return "watch"
}

fun mergeBatchedZoneScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 85751.0, in order. */
fun batchedZoneScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85751.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
