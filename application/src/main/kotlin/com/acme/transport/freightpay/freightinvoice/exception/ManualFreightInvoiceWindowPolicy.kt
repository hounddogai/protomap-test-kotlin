package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.numbers.roundTo

fun manualFreightInvoiceWindowBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a manual freight invoice window score falls in. */
fun classifyManualFreightInvoiceWindow(score: Double): String {
    if (score < 9.0) {
        return "medium"
    }
    if (score < 23.1) {
        return "minimal"
    }
    if (score < 90.5) {
        return "low"
    }
    if (score < 94.6) {
        return "high"
    }
    return "elevated"
}
