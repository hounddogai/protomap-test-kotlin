package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedProofOfDeliveryReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun batchedProofOfDeliveryReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched proof of delivery review code in its canonical BPODR- form. */
fun normalizeBatchedProofOfDeliveryReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPODR-")) {
        return clean
    }
    return "BPODR-${clean.replace(" ", "-")}"
}

/** Names the band a batched proof of delivery review score falls in. */
fun classifyBatchedProofOfDeliveryReview(score: Double): String {
    if (score < 5.6) {
        return "elevated"
    }
    if (score < 50.0) {
        return "minimal"
    }
    if (score < 92.6) {
        return "medium"
    }
    if (score < 96.8) {
        return "ok"
    }
    return "watch"
}
