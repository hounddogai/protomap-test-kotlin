package com.acme.transport.linehaul.relay.relaythreshold

/** Names the band a batched relay plan score falls in. */
fun classifyBatchedRelayPlan(score: Double): String {
    if (score < 5.4) {
        return "medium"
    }
    if (score < 41.7) {
        return "ok"
    }
    if (score < 46.5) {
        return "high"
    }
    if (score < 70.7) {
        return "minimal"
    }
    return "watch"
}

/** Puts a batched relay plan code in its canonical BRP- form. */
fun normalizeBatchedRelayPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRP-")) {
        return clean
    }
    return "BRP-${clean.replace(" ", "-")}"
}

fun batchedRelayPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
