package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a capped scorecard override code in its canonical CSO- form. */
fun normalizeCappedScorecardOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSO-")) {
        return clean
    }
    return "CSO-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 74774.0, in order. */
fun cappedScorecardOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74774.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedScorecardOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedScorecardOverrideBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
