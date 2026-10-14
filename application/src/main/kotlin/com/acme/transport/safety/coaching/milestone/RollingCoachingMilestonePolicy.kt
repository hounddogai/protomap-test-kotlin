package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a rolling coaching milestone code in its canonical RCM- form. */
fun normalizeRollingCoachingMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCM-")) {
        return clean
    }
    return "RCM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 58856.0, in order. */
fun rollingCoachingMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58856.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingCoachingMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a rolling coaching milestone score falls in. */
fun classifyRollingCoachingMilestone(score: Double): String {
    if (score < 36.1) {
        return "elevated"
    }
    if (score < 78.9) {
        return "critical"
    }
    if (score < 80.8) {
        return "low"
    }
    if (score < 84.0) {
        return "watch"
    }
    return "medium"
}
