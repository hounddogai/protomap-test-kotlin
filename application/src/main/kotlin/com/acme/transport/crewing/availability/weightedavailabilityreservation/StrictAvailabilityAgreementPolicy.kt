package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.numbers.roundTo

fun strictAvailabilityAgreementBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a strict availability agreement code in its canonical SAA- form. */
fun normalizeStrictAvailabilityAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAA-")) {
        return clean
    }
    return "SAA-${clean.replace(" ", "-")}"
}
