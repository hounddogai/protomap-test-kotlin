package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedFreightInvoiceOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched freight invoice override score falls in. */
fun classifyBatchedFreightInvoiceOverride(score: Double): String {
    if (score < 4.1) {
        return "elevated"
    }
    if (score < 29.0) {
        return "ok"
    }
    if (score < 85.3) {
        return "medium"
    }
    if (score < 86.3) {
        return "critical"
    }
    return "watch"
}
