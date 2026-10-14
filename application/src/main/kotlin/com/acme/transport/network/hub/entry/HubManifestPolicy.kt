package com.acme.transport.network.hub.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 14659.0, in order. */
fun hubManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14659.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun hubManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a hub manifest score falls in. */
fun classifyHubManifest(score: Double): String {
    if (score < 31.1) {
        return "high"
    }
    if (score < 80.9) {
        return "low"
    }
    if (score < 83.5) {
        return "watch"
    }
    return "medium"
}
