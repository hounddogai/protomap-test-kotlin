package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched scorecard threshold code in its canonical BST- form. */
fun normalizeBatchedScorecardThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BST-")) {
        return clean
    }
    return "BST-${clean.replace(" ", "-")}"
}

fun batchedScorecardThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a batched scorecard threshold score falls in. */
fun classifyBatchedScorecardThreshold(score: Double): String {
    if (score < 84.9) {
        return "watch"
    }
    if (score < 85.5) {
        return "critical"
    }
    return "elevated"
}
