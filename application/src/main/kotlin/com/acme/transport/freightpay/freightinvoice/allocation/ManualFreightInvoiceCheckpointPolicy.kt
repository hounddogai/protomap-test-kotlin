package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.numbers.roundTo

fun manualFreightInvoiceCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a manual freight invoice checkpoint score falls in. */
fun classifyManualFreightInvoiceCheckpoint(score: Double): String {
    if (score < 40.8) {
        return "critical"
    }
    if (score < 49.9) {
        return "watch"
    }
    if (score < 60.2) {
        return "high"
    }
    if (score < 69.0) {
        return "minimal"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 97503.0, in order. */
fun manualFreightInvoiceCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97503.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
