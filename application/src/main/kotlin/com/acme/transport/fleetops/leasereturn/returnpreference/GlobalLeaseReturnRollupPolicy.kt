package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalLeaseReturnRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 27414.0, in order. */
fun globalLeaseReturnRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27414.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalLeaseReturnRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a global lease return rollup score falls in. */
fun classifyGlobalLeaseReturnRollup(score: Double): String {
    if (score < 5.4) {
        return "minimal"
    }
    if (score < 38.9) {
        return "ok"
    }
    if (score < 71.8) {
        return "high"
    }
    return "watch"
}
