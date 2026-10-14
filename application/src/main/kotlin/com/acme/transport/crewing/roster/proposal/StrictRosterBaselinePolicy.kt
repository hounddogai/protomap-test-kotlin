package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a strict roster baseline code in its canonical SRB- form. */
fun normalizeStrictRosterBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRB-")) {
        return clean
    }
    return "SRB-${clean.replace(" ", "-")}"
}

/** Names the band a strict roster baseline score falls in. */
fun classifyStrictRosterBaseline(score: Double): String {
    if (score < 14.6) {
        return "elevated"
    }
    if (score < 60.4) {
        return "minimal"
    }
    if (score < 84.4) {
        return "low"
    }
    if (score < 96.6) {
        return "high"
    }
    return "ok"
}

fun strictRosterBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
