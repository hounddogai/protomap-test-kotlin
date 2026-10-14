package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 90104.0, in order. */
fun seasonalDisputeReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90104.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a seasonal dispute reservation score falls in. */
fun classifySeasonalDisputeReservation(score: Double): String {
    if (score < 11.9) {
        return "low"
    }
    if (score < 91.3) {
        return "critical"
    }
    if (score < 92.2) {
        return "watch"
    }
    return "elevated"
}

fun seasonalDisputeReservationBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a seasonal dispute reservation code in its canonical SDR- form. */
fun normalizeSeasonalDisputeReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDR-")) {
        return clean
    }
    return "SDR-${clean.replace(" ", "-")}"
}
