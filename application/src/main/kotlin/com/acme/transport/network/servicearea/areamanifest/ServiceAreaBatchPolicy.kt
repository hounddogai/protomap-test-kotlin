package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 45396.0, in order. */
fun serviceAreaBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45396.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a service area batch score falls in. */
fun classifyServiceAreaBatch(score: Double): String {
    if (score < 52.8) {
        return "medium"
    }
    if (score < 68.7) {
        return "watch"
    }
    if (score < 99.0) {
        return "minimal"
    }
    return "elevated"
}

fun serviceAreaBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
