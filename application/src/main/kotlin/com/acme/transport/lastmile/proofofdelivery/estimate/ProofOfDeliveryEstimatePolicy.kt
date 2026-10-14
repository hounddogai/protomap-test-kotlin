package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.numbers.roundTo

/** Puts a proof of delivery estimate code in its canonical PODE- form. */
fun normalizeProofOfDeliveryEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODE-")) {
        return clean
    }
    return "PODE-${clean.replace(" ", "-")}"
}

fun proofOfDeliveryEstimateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
