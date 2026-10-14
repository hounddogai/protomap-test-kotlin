package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a scheduled drop forecast score falls in. */
fun classifyScheduledDropForecast(score: Double): String {
    if (score < 24.3) {
        return "minimal"
    }
    if (score < 35.0) {
        return "watch"
    }
    if (score < 53.6) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 39065.0, in order. */
fun scheduledDropForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39065.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledDropForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
