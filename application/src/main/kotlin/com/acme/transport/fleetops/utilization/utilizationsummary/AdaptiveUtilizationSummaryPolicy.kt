package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.numbers.roundTo

/** Puts a adaptive utilization summary code in its canonical AUS- form. */
fun normalizeAdaptiveUtilizationSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AUS-")) {
        return clean
    }
    return "AUS-${clean.replace(" ", "-")}"
}

fun adaptiveUtilizationSummaryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
