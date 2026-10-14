package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a tiered sortation checkpoint score falls in. */
fun classifyTieredSortationCheckpoint(score: Double): String {
    if (score < 73.4) {
        return "medium"
    }
    if (score < 81.3) {
        return "watch"
    }
    return "critical"
}

/** Puts a tiered sortation checkpoint code in its canonical TSC- form. */
fun normalizeTieredSortationCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSC-")) {
        return clean
    }
    return "TSC-${clean.replace(" ", "-")}"
}

fun tieredSortationCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
