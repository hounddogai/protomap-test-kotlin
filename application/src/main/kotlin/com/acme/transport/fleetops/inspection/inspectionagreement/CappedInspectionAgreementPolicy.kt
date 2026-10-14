package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.numbers.roundTo

fun cappedInspectionAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun cappedInspectionAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
