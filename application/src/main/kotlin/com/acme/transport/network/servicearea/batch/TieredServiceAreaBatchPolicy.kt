package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a tiered service area batch code in its canonical TSAB- form. */
fun normalizeTieredServiceAreaBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSAB-")) {
        return clean
    }
    return "TSAB-${clean.replace(" ", "-")}"
}

fun tieredServiceAreaBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered service area batch score falls in. */
fun classifyTieredServiceAreaBatch(score: Double): String {
    if (score < 46.9) {
        return "elevated"
    }
    if (score < 62.8) {
        return "minimal"
    }
    return "critical"
}

fun tieredServiceAreaBatchBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
