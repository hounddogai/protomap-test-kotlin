package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.numbers.roundTo

fun qualificationMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a qualification milestone code in its canonical QM- form. */
fun normalizeQualificationMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QM-")) {
        return clean
    }
    return "QM-${clean.replace(" ", "-")}"
}
