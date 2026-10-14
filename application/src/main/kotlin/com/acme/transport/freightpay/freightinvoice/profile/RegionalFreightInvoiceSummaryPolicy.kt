package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalFreightInvoiceSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun regionalFreightInvoiceSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional freight invoice summary score falls in. */
fun classifyRegionalFreightInvoiceSummary(score: Double): String {
    if (score < 23.7) {
        return "elevated"
    }
    if (score < 36.7) {
        return "critical"
    }
    if (score < 37.1) {
        return "medium"
    }
    if (score < 95.0) {
        return "low"
    }
    return "watch"
}
