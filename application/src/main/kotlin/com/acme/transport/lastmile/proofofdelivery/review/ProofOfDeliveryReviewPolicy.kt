package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.numbers.roundTo

fun proofOfDeliveryReviewBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a proof of delivery review code in its canonical PODR- form. */
fun normalizeProofOfDeliveryReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODR-")) {
        return clean
    }
    return "PODR-${clean.replace(" ", "-")}"
}

/** Names the band a proof of delivery review score falls in. */
fun classifyProofOfDeliveryReview(score: Double): String {
    if (score < 33.0) {
        return "high"
    }
    if (score < 50.4) {
        return "medium"
    }
    if (score < 66.8) {
        return "minimal"
    }
    if (score < 94.7) {
        return "ok"
    }
    return "elevated"
}
