package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedTransitTimeRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched transit time rule score falls in. */
fun classifyBatchedTransitTimeRule(score: Double): String {
    if (score < 11.7) {
        return "ok"
    }
    if (score < 56.9) {
        return "critical"
    }
    return "minimal"
}

/** Puts a batched transit time rule code in its canonical BTTR- form. */
fun normalizeBatchedTransitTimeRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTTR-")) {
        return clean
    }
    return "BTTR-${clean.replace(" ", "-")}"
}
