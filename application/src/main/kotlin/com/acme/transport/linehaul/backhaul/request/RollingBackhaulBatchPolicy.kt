package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingBackhaulBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rolling backhaul batch code in its canonical RBB- form. */
fun normalizeRollingBackhaulBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBB-")) {
        return clean
    }
    return "RBB-${clean.replace(" ", "-")}"
}

/** Names the band a rolling backhaul batch score falls in. */
fun classifyRollingBackhaulBatch(score: Double): String {
    if (score < 44.4) {
        return "watch"
    }
    if (score < 67.5) {
        return "elevated"
    }
    if (score < 69.3) {
        return "medium"
    }
    return "ok"
}
