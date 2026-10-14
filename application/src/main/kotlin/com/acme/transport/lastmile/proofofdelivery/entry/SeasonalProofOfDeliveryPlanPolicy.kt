package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.numbers.roundTo

fun seasonalProofOfDeliveryPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a seasonal proof of delivery plan code in its canonical SPODP- form. */
fun normalizeSeasonalProofOfDeliveryPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPODP-")) {
        return clean
    }
    return "SPODP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 79089.0, in order. */
fun seasonalProofOfDeliveryPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79089.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalProofOfDeliveryPlanBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
