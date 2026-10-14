package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dockDoorCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a dock door checkpoint score falls in. */
fun classifyDockDoorCheckpoint(score: Double): String {
    if (score < 1.9) {
        return "medium"
    }
    if (score < 51.4) {
        return "low"
    }
    if (score < 71.5) {
        return "minimal"
    }
    if (score < 74.5) {
        return "high"
    }
    return "critical"
}

fun dockDoorCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a dock door checkpoint code in its canonical DDC- form. */
fun normalizeDockDoorCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDC-")) {
        return clean
    }
    return "DDC-${clean.replace(" ", "-")}"
}
