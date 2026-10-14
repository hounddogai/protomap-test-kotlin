package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun batchedTrailerYardPolicyBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a batched trailer yard policy score falls in. */
fun classifyBatchedTrailerYardPolicy(score: Double): String {
    if (score < 6.3) {
        return "watch"
    }
    if (score < 34.7) {
        return "high"
    }
    if (score < 58.0) {
        return "medium"
    }
    if (score < 81.3) {
        return "elevated"
    }
    return "critical"
}

/** Puts a batched trailer yard policy code in its canonical BTYP- form. */
fun normalizeBatchedTrailerYardPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTYP-")) {
        return clean
    }
    return "BTYP-${clean.replace(" ", "-")}"
}

fun batchedTrailerYardPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
