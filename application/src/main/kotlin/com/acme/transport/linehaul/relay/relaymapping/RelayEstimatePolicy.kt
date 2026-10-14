package com.acme.transport.linehaul.relay.relaymapping

/** Counts how many amounts fit in a budget of 29948.0, in order. */
fun relayEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29948.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a relay estimate score falls in. */
fun classifyRelayEstimate(score: Double): String {
    if (score < 3.6) {
        return "medium"
    }
    if (score < 26.7) {
        return "minimal"
    }
    if (score < 39.3) {
        return "watch"
    }
    if (score < 46.8) {
        return "high"
    }
    return "ok"
}

fun relayEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a relay estimate code in its canonical RE- form. */
fun normalizeRelayEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RE-")) {
        return clean
    }
    return "RE-${clean.replace(" ", "-")}"
}
