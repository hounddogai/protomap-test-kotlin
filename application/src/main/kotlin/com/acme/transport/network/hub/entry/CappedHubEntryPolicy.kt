package com.acme.transport.network.hub.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a capped hub entry score falls in. */
fun classifyCappedHubEntry(score: Double): String {
    if (score < 12.8) {
        return "ok"
    }
    if (score < 54.7) {
        return "critical"
    }
    if (score < 63.6) {
        return "low"
    }
    if (score < 83.0) {
        return "high"
    }
    return "medium"
}

fun cappedHubEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedHubEntryBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 75927.0, in order. */
fun cappedHubEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75927.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
