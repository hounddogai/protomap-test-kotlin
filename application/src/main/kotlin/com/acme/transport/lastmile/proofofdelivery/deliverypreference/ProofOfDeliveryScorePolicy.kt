package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.numbers.roundTo

/** Names the band a proof of delivery score score falls in. */
fun classifyProofOfDeliveryScore(score: Double): String {
    if (score < 29.8) {
        return "watch"
    }
    if (score < 61.0) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 33391.0, in order. */
fun proofOfDeliveryScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33391.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a proof of delivery score code in its canonical PODS- form. */
fun normalizeProofOfDeliveryScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODS-")) {
        return clean
    }
    return "PODS-${clean.replace(" ", "-")}"
}

fun proofOfDeliveryScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
