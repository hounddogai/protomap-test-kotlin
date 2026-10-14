package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a dock door override code in its canonical DDO- form. */
fun normalizeDockDoorOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDO-")) {
        return clean
    }
    return "DDO-${clean.replace(" ", "-")}"
}

fun dockDoorOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a dock door override score falls in. */
fun classifyDockDoorOverride(score: Double): String {
    if (score < 16.2) {
        return "minimal"
    }
    if (score < 25.0) {
        return "medium"
    }
    return "critical"
}
