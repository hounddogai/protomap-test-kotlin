package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a roster assignment code in its canonical RA- form. */
fun normalizeRosterAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}

fun rosterAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
