package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.numbers.roundTo

fun rollingFreightInvoiceBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun rollingFreightInvoiceBaselineBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
