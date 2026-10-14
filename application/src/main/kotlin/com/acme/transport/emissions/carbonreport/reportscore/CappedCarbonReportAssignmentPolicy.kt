package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.numbers.roundTo

/** Puts a capped carbon report assignment code in its canonical CCRA- form. */
fun normalizeCappedCarbonReportAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCRA-")) {
        return clean
    }
    return "CCRA-${clean.replace(" ", "-")}"
}

fun cappedCarbonReportAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
