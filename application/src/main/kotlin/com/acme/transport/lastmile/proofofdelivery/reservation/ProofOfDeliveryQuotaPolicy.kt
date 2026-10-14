package com.acme.transport.lastmile.proofofdelivery.reservation

/** Puts a proof of delivery quota code in its canonical PODQ- form. */
fun normalizeProofOfDeliveryQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODQ-")) {
        return clean
    }
    return "PODQ-${clean.replace(" ", "-")}"
}

fun proofOfDeliveryQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
