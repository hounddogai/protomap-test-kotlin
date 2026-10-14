package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a parcel review code in its canonical PR- form. */
fun normalizeParcelReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PR-")) {
        return clean
    }
    return "PR-${clean.replace(" ", "-")}"
}

fun parcelReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelReview(): Int {
    val policy = defaultPolicy("parcel-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a parcel review score falls in. */
fun classifyParcelReview(score: Double): String {
    if (score < 14.2) {
        return "high"
    }
    if (score < 23.4) {
        return "elevated"
    }
    if (score < 73.9) {
        return "low"
    }
    return "critical"
}
