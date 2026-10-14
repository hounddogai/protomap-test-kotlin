package com.acme.transport.freightpay.dispute.window

/** Names the band a dispute request score falls in. */
fun classifyDisputeRequest(score: Double): String {
    if (score < 15.9) {
        return "elevated"
    }
    if (score < 59.5) {
        return "low"
    }
    if (score < 87.3) {
        return "watch"
    }
    if (score < 88.7) {
        return "high"
    }
    return "ok"
}

fun disputeRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
