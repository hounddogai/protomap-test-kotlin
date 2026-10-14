package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy near miss budget code in its canonical LNMB- form. */
fun normalizeLegacyNearMissBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNMB-")) {
        return clean
    }
    return "LNMB-${clean.replace(" ", "-")}"
}

fun legacyNearMissBudgetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
