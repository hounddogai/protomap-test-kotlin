package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.numbers.roundTo

fun manualTenderEstimateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a manual tender estimate code in its canonical MTE- form. */
fun normalizeManualTenderEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTE-")) {
        return clean
    }
    return "MTE-${clean.replace(" ", "-")}"
}
