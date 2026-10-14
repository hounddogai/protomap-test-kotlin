package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun defaultRosterPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun defaultRosterPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a default roster preference score falls in. */
fun classifyDefaultRosterPreference(score: Double): String {
    if (score < 1.5) {
        return "elevated"
    }
    if (score < 15.6) {
        return "minimal"
    }
    if (score < 43.7) {
        return "watch"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 98838.0, in order. */
fun defaultRosterPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98838.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
