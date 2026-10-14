package com.acme.transport.customs.broker.batch

/** Puts a broker batch code in its canonical BB- form. */
fun normalizeBrokerBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BB-")) {
        return clean
    }
    return "BB-${clean.replace(" ", "-")}"
}

/** Names the band a broker batch score falls in. */
fun classifyBrokerBatch(score: Double): String {
    if (score < 2.2) {
        return "low"
    }
    if (score < 82.2) {
        return "critical"
    }
    return "watch"
}
