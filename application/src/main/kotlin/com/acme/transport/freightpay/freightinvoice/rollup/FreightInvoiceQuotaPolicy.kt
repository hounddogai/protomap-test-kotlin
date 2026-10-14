package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun freightInvoiceQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a freight invoice quota score falls in. */
fun classifyFreightInvoiceQuota(score: Double): String {
    if (score < 47.8) {
        return "ok"
    }
    if (score < 65.4) {
        return "elevated"
    }
    if (score < 79.4) {
        return "minimal"
    }
    if (score < 95.4) {
        return "high"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 93983.0, in order. */
fun freightInvoiceQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93983.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
