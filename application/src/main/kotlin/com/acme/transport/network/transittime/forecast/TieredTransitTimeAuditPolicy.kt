package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tiered transit time audit code in its canonical TTTA- form. */
fun normalizeTieredTransitTimeAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTTA-")) {
        return clean
    }
    return "TTTA-${clean.replace(" ", "-")}"
}

/** Names the band a tiered transit time audit score falls in. */
fun classifyTieredTransitTimeAudit(score: Double): String {
    if (score < 42.8) {
        return "high"
    }
    if (score < 54.2) {
        return "critical"
    }
    if (score < 57.6) {
        return "watch"
    }
    if (score < 93.6) {
        return "ok"
    }
    return "low"
}

fun tieredTransitTimeAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun tieredTransitTimeAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
