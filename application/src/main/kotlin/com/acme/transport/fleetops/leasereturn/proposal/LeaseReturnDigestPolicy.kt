package com.acme.transport.fleetops.leasereturn.proposal

/** Names the band a lease return digest score falls in. */
fun classifyLeaseReturnDigest(score: Double): String {
    if (score < 45.5) {
        return "low"
    }
    if (score < 51.9) {
        return "high"
    }
    return "medium"
}

fun leaseReturnDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a lease return digest code in its canonical LRD- form. */
fun normalizeLeaseReturnDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRD-")) {
        return clean
    }
    return "LRD-${clean.replace(" ", "-")}"
}
