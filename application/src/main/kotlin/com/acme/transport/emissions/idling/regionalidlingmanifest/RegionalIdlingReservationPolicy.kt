package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 287.0, in order. */
fun regionalIdlingReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 287.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a regional idling reservation score falls in. */
fun classifyRegionalIdlingReservation(score: Double): String {
    if (score < 42.8) {
        return "minimal"
    }
    if (score < 63.6) {
        return "watch"
    }
    if (score < 83.1) {
        return "critical"
    }
    return "low"
}

fun regionalIdlingReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
