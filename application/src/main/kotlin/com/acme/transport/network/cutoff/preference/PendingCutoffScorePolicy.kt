package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.numbers.roundTo

fun pendingCutoffScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a pending cutoff score code in its canonical PCS- form. */
fun normalizePendingCutoffScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCS-")) {
        return clean
    }
    return "PCS-${clean.replace(" ", "-")}"
}
