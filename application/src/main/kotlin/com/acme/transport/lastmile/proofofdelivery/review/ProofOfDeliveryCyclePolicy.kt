package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.numbers.roundTo

fun proofOfDeliveryCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a proof of delivery cycle code in its canonical PODC- form. */
fun normalizeProofOfDeliveryCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODC-")) {
        return clean
    }
    return "PODC-${clean.replace(" ", "-")}"
}
