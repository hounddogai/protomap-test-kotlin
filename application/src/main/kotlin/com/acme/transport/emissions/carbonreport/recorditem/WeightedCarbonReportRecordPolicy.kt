package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.numbers.roundTo

fun weightedCarbonReportRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun weightedCarbonReportRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
