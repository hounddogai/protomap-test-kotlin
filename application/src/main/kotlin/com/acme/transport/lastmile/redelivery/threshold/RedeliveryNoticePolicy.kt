package com.acme.transport.lastmile.redelivery.threshold

/** Puts a redelivery notice code in its canonical RN- form. */
fun normalizeRedeliveryNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RN-")) {
        return clean
    }
    return "RN-${clean.replace(" ", "-")}"
}

/** Names the band a redelivery notice score falls in. */
fun classifyRedeliveryNotice(score: Double): String {
    if (score < 32.4) {
        return "elevated"
    }
    if (score < 34.6) {
        return "critical"
    }
    return "ok"
}
