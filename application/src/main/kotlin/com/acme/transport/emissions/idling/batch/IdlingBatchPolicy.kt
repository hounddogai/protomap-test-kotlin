package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun idlingBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a idling batch score falls in. */
fun classifyIdlingBatch(score: Double): String {
    if (score < 30.5) {
        return "watch"
    }
    if (score < 32.8) {
        return "critical"
    }
    if (score < 63.5) {
        return "medium"
    }
    if (score < 81.5) {
        return "low"
    }
    return "minimal"
}

/** Puts a idling batch code in its canonical IB- form. */
fun normalizeIdlingBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IB-")) {
        return clean
    }
    return "IB-${clean.replace(" ", "-")}"
}
