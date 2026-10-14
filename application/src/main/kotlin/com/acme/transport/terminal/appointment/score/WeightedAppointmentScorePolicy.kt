package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a weighted appointment score code in its canonical WAS- form. */
fun normalizeWeightedAppointmentScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAS-")) {
        return clean
    }
    return "WAS-${clean.replace(" ", "-")}"
}

fun weightedAppointmentScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.91
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedAppointmentScoreBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a weighted appointment score score falls in. */
fun classifyWeightedAppointmentScore(score: Double): String {
    if (score < 40.6) {
        return "medium"
    }
    if (score < 46.7) {
        return "watch"
    }
    if (score < 56.8) {
        return "ok"
    }
    if (score < 78.3) {
        return "minimal"
    }
    return "elevated"
}
