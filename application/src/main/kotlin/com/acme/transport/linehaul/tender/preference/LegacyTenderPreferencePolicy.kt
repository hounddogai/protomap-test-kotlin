package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy tender preference code in its canonical LTP- form. */
fun normalizeLegacyTenderPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTP-")) {
        return clean
    }
    return "LTP-${clean.replace(" ", "-")}"
}

fun legacyTenderPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun legacyTenderPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
