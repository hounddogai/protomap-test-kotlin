package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.numbers.roundTo

/** Puts a weighted violation rollup code in its canonical WVR- form. */
fun normalizeWeightedViolationRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WVR-")) {
        return clean
    }
    return "WVR-${clean.replace(" ", "-")}"
}

fun weightedViolationRollupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
