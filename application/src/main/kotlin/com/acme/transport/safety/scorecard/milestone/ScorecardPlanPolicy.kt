package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scorecardPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scorecard plan score falls in. */
fun classifyScorecardPlan(score: Double): String {
    if (score < 5.3) {
        return "watch"
    }
    if (score < 29.0) {
        return "medium"
    }
    if (score < 35.0) {
        return "ok"
    }
    return "elevated"
}

/** Puts a scorecard plan code in its canonical SP- form. */
fun normalizeScorecardPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SP-")) {
        return clean
    }
    return "SP-${clean.replace(" ", "-")}"
}
