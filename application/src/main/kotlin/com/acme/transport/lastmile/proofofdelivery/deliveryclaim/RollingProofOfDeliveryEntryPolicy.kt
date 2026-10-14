package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.numbers.roundTo

/** Puts a rolling proof of delivery entry code in its canonical RPODE- form. */
fun normalizeRollingProofOfDeliveryEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPODE-")) {
        return clean
    }
    return "RPODE-${clean.replace(" ", "-")}"
}

fun rollingProofOfDeliveryEntryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a rolling proof of delivery entry score falls in. */
fun classifyRollingProofOfDeliveryEntry(score: Double): String {
    if (score < 25.6) {
        return "high"
    }
    if (score < 38.8) {
        return "ok"
    }
    if (score < 73.8) {
        return "medium"
    }
    return "low"
}
