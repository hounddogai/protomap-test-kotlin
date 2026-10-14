package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun leaseReturnProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun leaseReturnProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lease return profile score falls in. */
fun classifyLeaseReturnProfile(score: Double): String {
    if (score < 4.6) {
        return "ok"
    }
    if (score < 86.1) {
        return "low"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 2764.0, in order. */
fun leaseReturnProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2764.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
