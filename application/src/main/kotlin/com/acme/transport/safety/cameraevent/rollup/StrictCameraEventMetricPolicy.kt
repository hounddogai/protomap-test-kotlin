package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a strict camera event metric code in its canonical SCEM- form. */
fun normalizeStrictCameraEventMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCEM-")) {
        return clean
    }
    return "SCEM-${clean.replace(" ", "-")}"
}

/** Names the band a strict camera event metric score falls in. */
fun classifyStrictCameraEventMetric(score: Double): String {
    if (score < 33.1) {
        return "high"
    }
    if (score < 46.9) {
        return "minimal"
    }
    if (score < 54.2) {
        return "medium"
    }
    if (score < 54.5) {
        return "watch"
    }
    return "elevated"
}

fun strictCameraEventMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictCameraEventMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
