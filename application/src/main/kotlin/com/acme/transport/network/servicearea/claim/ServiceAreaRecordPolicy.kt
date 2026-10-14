package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun serviceAreaRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a service area record code in its canonical SAR- form. */
fun normalizeServiceAreaRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAR-")) {
        return clean
    }
    return "SAR-${clean.replace(" ", "-")}"
}

fun serviceAreaRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a service area record score falls in. */
fun classifyServiceAreaRecord(score: Double): String {
    if (score < 1.6) {
        return "elevated"
    }
    if (score < 9.3) {
        return "critical"
    }
    if (score < 20.9) {
        return "high"
    }
    if (score < 80.1) {
        return "minimal"
    }
    return "low"
}
