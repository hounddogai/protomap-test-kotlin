package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.numbers.roundTo

/** Puts a scheduled proof of delivery digest code in its canonical SPODD- form. */
fun normalizeScheduledProofOfDeliveryDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPODD-")) {
        return clean
    }
    return "SPODD-${clean.replace(" ", "-")}"
}

fun scheduledProofOfDeliveryDigestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
