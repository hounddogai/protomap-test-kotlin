package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a lenient swap group score falls in. */
fun classifyLenientSwapGroup(score: Double): String {
    if (score < 10.0) {
        return "critical"
    }
    if (score < 53.4) {
        return "elevated"
    }
    return "ok"
}

fun lenientSwapGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientSwapGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a lenient swap group code in its canonical LSG- form. */
fun normalizeLenientSwapGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSG-")) {
        return clean
    }
    return "LSG-${clean.replace(" ", "-")}"
}
