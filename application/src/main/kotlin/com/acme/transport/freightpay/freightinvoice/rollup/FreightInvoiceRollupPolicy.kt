package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a freight invoice rollup score falls in. */
fun classifyFreightInvoiceRollup(score: Double): String {
    if (score < 7.0) {
        return "watch"
    }
    if (score < 10.0) {
        return "high"
    }
    return "elevated"
}

fun freightInvoiceRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun freightInvoiceRollupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun freightInvoiceRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
