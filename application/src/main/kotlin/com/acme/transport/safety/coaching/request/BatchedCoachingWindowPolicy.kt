package com.acme.transport.safety.coaching.request

/** Puts a batched coaching window code in its canonical BCW- form. */
fun normalizeBatchedCoachingWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BCW-")) {
        return clean
    }
    return "BCW-${clean.replace(" ", "-")}"
}

fun batchedCoachingWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a batched coaching window score falls in. */
fun classifyBatchedCoachingWindow(score: Double): String {
    if (score < 28.0) {
        return "watch"
    }
    if (score < 42.4) {
        return "low"
    }
    if (score < 86.5) {
        return "elevated"
    }
    return "medium"
}
