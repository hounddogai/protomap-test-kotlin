package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 56179.0, in order. */
fun batchedBackhaulDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56179.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched backhaul digest score falls in. */
fun classifyBatchedBackhaulDigest(score: Double): String {
    if (score < 7.4) {
        return "medium"
    }
    if (score < 18.9) {
        return "watch"
    }
    if (score < 92.7) {
        return "critical"
    }
    return "elevated"
}

/** Puts a batched backhaul digest code in its canonical BBD- form. */
fun normalizeBatchedBackhaulDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BBD-")) {
        return clean
    }
    return "BBD-${clean.replace(" ", "-")}"
}

fun batchedBackhaulDigestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
