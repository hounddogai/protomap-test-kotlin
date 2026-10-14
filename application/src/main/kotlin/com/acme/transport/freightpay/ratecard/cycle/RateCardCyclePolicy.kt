package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rateCardCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun rateCardCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rate card cycle code in its canonical RCC- form. */
fun normalizeRateCardCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCC-")) {
        return clean
    }
    return "RCC-${clean.replace(" ", "-")}"
}

/** Names the band a rate card cycle score falls in. */
fun classifyRateCardCycle(score: Double): String {
    if (score < 48.8) {
        return "watch"
    }
    if (score < 58.2) {
        return "critical"
    }
    if (score < 59.9) {
        return "elevated"
    }
    return "ok"
}
