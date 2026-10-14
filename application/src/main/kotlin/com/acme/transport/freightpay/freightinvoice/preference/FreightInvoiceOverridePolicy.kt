package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 91466.0, in order. */
fun freightInvoiceOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91466.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun freightInvoiceOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a freight invoice override score falls in. */
fun classifyFreightInvoiceOverride(score: Double): String {
    if (score < 20.5) {
        return "high"
    }
    if (score < 34.8) {
        return "medium"
    }
    return "low"
}
