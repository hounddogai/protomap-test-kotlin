package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun weightedNearMissEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted near miss estimate score falls in. */
fun classifyWeightedNearMissEstimate(score: Double): String {
    if (score < 10.2) {
        return "medium"
    }
    if (score < 26.3) {
        return "low"
    }
    if (score < 41.0) {
        return "ok"
    }
    return "high"
}

/** Puts a weighted near miss estimate code in its canonical WNME- form. */
fun normalizeWeightedNearMissEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WNME-")) {
        return clean
    }
    return "WNME-${clean.replace(" ", "-")}"
}
