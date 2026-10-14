package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a lease return preference score falls in. */
fun classifyLeaseReturnPreference(score: Double): String {
    if (score < 42.9) {
        return "critical"
    }
    if (score < 66.9) {
        return "medium"
    }
    if (score < 81.7) {
        return "minimal"
    }
    if (score < 86.3) {
        return "high"
    }
    return "elevated"
}

fun leaseReturnPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 47625.0, in order. */
fun leaseReturnPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47625.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun leaseReturnPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
