package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tariffCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun tariffCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tariff checkpoint score falls in. */
fun classifyTariffCheckpoint(score: Double): String {
    if (score < 18.7) {
        return "high"
    }
    if (score < 40.2) {
        return "elevated"
    }
    if (score < 85.0) {
        return "watch"
    }
    if (score < 99.0) {
        return "low"
    }
    return "critical"
}

/** Puts a tariff checkpoint code in its canonical TC- form. */
fun normalizeTariffCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TC-")) {
        return clean
    }
    return "TC-${clean.replace(" ", "-")}"
}
