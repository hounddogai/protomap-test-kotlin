package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a seasonal tire milestone score falls in. */
fun classifySeasonalTireMilestone(score: Double): String {
    if (score < 73.6) {
        return "elevated"
    }
    if (score < 84.7) {
        return "low"
    }
    return "watch"
}

/** Puts a seasonal tire milestone code in its canonical STM- form. */
fun normalizeSeasonalTireMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STM-")) {
        return clean
    }
    return "STM-${clean.replace(" ", "-")}"
}

fun seasonalTireMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
