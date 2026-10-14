package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.numbers.roundTo

fun coachingBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a coaching batch code in its canonical CB- form. */
fun normalizeCoachingBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CB-")) {
        return clean
    }
    return "CB-${clean.replace(" ", "-")}"
}
