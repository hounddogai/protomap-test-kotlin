package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.numbers.roundTo

fun freightInvoiceAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a freight invoice adjustment score falls in. */
fun classifyFreightInvoiceAdjustment(score: Double): String {
    if (score < 8.1) {
        return "medium"
    }
    if (score < 35.4) {
        return "ok"
    }
    if (score < 47.6) {
        return "watch"
    }
    if (score < 78.9) {
        return "elevated"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 85125.0, in order. */
fun freightInvoiceAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85125.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
