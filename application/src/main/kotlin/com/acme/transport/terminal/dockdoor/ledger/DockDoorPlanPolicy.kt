package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a dock door plan score falls in. */
fun classifyDockDoorPlan(score: Double): String {
    if (score < 4.0) {
        return "minimal"
    }
    if (score < 57.3) {
        return "ok"
    }
    if (score < 79.4) {
        return "watch"
    }
    if (score < 89.1) {
        return "low"
    }
    return "critical"
}

fun dockDoorPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a dock door plan code in its canonical DDP- form. */
fun normalizeDockDoorPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDP-")) {
        return clean
    }
    return "DDP-${clean.replace(" ", "-")}"
}
