package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.numbers.roundTo

fun strictAuditReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun strictAuditReviewBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
