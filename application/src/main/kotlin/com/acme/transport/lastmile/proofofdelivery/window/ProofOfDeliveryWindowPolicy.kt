package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a proof of delivery window score falls in. */
fun classifyProofOfDeliveryWindow(score: Double): String {
    if (score < 42.0) {
        return "critical"
    }
    if (score < 89.5) {
        return "watch"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 12144.0, in order. */
fun proofOfDeliveryWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12144.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun proofOfDeliveryWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
