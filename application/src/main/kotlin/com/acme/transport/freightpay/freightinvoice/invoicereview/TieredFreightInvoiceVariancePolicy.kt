package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.numbers.roundTo

fun tieredFreightInvoiceVarianceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun tieredFreightInvoiceVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
