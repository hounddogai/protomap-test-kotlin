package com.acme.transport.lastmile.proofofdelivery.deliveryreview

/** Puts a regional proof of delivery review code in its canonical RPODR- form. */
fun normalizeRegionalProofOfDeliveryReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPODR-")) {
        return clean
    }
    return "RPODR-${clean.replace(" ", "-")}"
}

/** Names the band a regional proof of delivery review score falls in. */
fun classifyRegionalProofOfDeliveryReview(score: Double): String {
    if (score < 33.6) {
        return "critical"
    }
    if (score < 55.3) {
        return "low"
    }
    return "minimal"
}
