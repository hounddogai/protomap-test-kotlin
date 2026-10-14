package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched ev charge score code in its canonical BECS- form. */
fun normalizeBatchedEvChargeScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BECS-")) {
        return clean
    }
    return "BECS-${clean.replace(" ", "-")}"
}

fun batchedEvChargeScoreBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
