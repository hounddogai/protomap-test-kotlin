package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.numbers.roundTo

fun defaultScorecardAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a default scorecard assignment code in its canonical DSA- form. */
fun normalizeDefaultScorecardAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DSA-")) {
        return clean
    }
    return "DSA-${clean.replace(" ", "-")}"
}
