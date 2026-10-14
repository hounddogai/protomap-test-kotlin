package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.numbers.roundTo

fun regionalFuelBurnRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun regionalFuelBurnRecordBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a regional fuel burn record code in its canonical RFBR- form. */
fun normalizeRegionalFuelBurnRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFBR-")) {
        return clean
    }
    return "RFBR-${clean.replace(" ", "-")}"
}
