package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.numbers.roundTo

fun primaryFreightInvoiceManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun primaryFreightInvoiceManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a primary freight invoice manifest score falls in. */
fun classifyPrimaryFreightInvoiceManifest(score: Double): String {
    if (score < 3.6) {
        return "minimal"
    }
    if (score < 28.8) {
        return "critical"
    }
    if (score < 32.1) {
        return "watch"
    }
    return "medium"
}
