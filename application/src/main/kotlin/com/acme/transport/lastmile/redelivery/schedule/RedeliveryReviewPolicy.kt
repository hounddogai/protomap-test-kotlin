package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a redelivery review score falls in. */
fun classifyRedeliveryReview(score: Double): String {
    if (score < 43.8) {
        return "ok"
    }
    if (score < 47.6) {
        return "medium"
    }
    if (score < 92.2) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryReview(): Int {
    val policy = defaultPolicy("redelivery-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a redelivery review code in its canonical RR- form. */
fun normalizeRedeliveryReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RR-")) {
        return clean
    }
    return "RR-${clean.replace(" ", "-")}"
}
