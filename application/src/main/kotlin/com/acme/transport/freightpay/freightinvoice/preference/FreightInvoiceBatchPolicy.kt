package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun freightInvoiceBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a freight invoice batch score falls in. */
fun classifyFreightInvoiceBatch(score: Double): String {
    if (score < 45.5) {
        return "watch"
    }
    if (score < 48.4) {
        return "ok"
    }
    return "minimal"
}

fun freightInvoiceBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun freightInvoiceBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
