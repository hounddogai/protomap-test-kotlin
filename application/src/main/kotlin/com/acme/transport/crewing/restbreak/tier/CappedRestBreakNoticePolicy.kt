package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.numbers.roundTo

fun cappedRestBreakNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a capped rest break notice code in its canonical CRBN- form. */
fun normalizeCappedRestBreakNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRBN-")) {
        return clean
    }
    return "CRBN-${clean.replace(" ", "-")}"
}
