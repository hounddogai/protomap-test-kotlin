package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedTireSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a capped tire summary code in its canonical CTS- form. */
fun normalizeCappedTireSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CTS-")) {
        return clean
    }
    return "CTS-${clean.replace(" ", "-")}"
}

/** Names the band a capped tire summary score falls in. */
fun classifyCappedTireSummary(score: Double): String {
    if (score < 3.1) {
        return "elevated"
    }
    if (score < 91.1) {
        return "high"
    }
    return "minimal"
}
