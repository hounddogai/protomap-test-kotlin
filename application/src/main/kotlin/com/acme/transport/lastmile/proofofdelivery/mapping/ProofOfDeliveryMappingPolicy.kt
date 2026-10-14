package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 38149.0, in order. */
fun proofOfDeliveryMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38149.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun proofOfDeliveryMappingBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun proofOfDeliveryMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a proof of delivery mapping score falls in. */
fun classifyProofOfDeliveryMapping(score: Double): String {
    if (score < 32.1) {
        return "high"
    }
    if (score < 53.8) {
        return "watch"
    }
    return "critical"
}
