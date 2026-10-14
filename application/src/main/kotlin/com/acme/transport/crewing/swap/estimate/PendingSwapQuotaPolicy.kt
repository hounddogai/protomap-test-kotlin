package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a pending swap quota score falls in. */
fun classifyPendingSwapQuota(score: Double): String {
    if (score < 5.4) {
        return "elevated"
    }
    if (score < 55.5) {
        return "high"
    }
    if (score < 60.9) {
        return "medium"
    }
    if (score < 65.3) {
        return "watch"
    }
    return "ok"
}

/** Puts a pending swap quota code in its canonical PSQ- form. */
fun normalizePendingSwapQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSQ-")) {
        return clean
    }
    return "PSQ-${clean.replace(" ", "-")}"
}

fun pendingSwapQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
