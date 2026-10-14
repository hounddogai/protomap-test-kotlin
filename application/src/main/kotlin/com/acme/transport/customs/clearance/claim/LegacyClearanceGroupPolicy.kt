package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy clearance group code in its canonical LCG- form. */
fun normalizeLegacyClearanceGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCG-")) {
        return clean
    }
    return "LCG-${clean.replace(" ", "-")}"
}

fun legacyClearanceGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
