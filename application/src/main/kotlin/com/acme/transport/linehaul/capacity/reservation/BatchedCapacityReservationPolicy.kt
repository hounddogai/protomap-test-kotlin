package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.numbers.roundTo

fun batchedCapacityReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a batched capacity reservation score falls in. */
fun classifyBatchedCapacityReservation(score: Double): String {
    if (score < 47.7) {
        return "minimal"
    }
    if (score < 66.3) {
        return "high"
    }
    return "ok"
}

fun batchedCapacityReservationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 30176.0, in order. */
fun batchedCapacityReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
