package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryFreightInvoiceAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary freight invoice adjustment score falls in. */
fun classifyPrimaryFreightInvoiceAdjustment(score: Double): String {
    if (score < 4.6) {
        return "high"
    }
    if (score < 94.5) {
        return "watch"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 51677.0, in order. */
fun primaryFreightInvoiceAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51677.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryFreightInvoiceAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
