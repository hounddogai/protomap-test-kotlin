package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.numbers.roundTo

/** Puts a pending carbon report milestone code in its canonical PCRM- form. */
fun normalizePendingCarbonReportMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCRM-")) {
        return clean
    }
    return "PCRM-${clean.replace(" ", "-")}"
}

fun pendingCarbonReportMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
