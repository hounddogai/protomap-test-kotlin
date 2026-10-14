package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a roster group code in its canonical RG- form. */
fun normalizeRosterGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RG-")) {
        return clean
    }
    return "RG-${clean.replace(" ", "-")}"
}

fun rosterGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a roster group score falls in. */
fun classifyRosterGroup(score: Double): String {
    if (score < 11.3) {
        return "low"
    }
    if (score < 31.8) {
        return "critical"
    }
    if (score < 82.9) {
        return "minimal"
    }
    if (score < 99.2) {
        return "high"
    }
    return "medium"
}
