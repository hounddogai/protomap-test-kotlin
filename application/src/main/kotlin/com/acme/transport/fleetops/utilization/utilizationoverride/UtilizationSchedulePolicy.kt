package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.numbers.roundTo

/** Puts a utilization schedule code in its canonical US- form. */
fun normalizeUtilizationScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("US-")) {
        return clean
    }
    return "US-${clean.replace(" ", "-")}"
}

fun utilizationScheduleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
