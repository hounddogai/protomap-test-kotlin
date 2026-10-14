package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a global trailer claim score falls in. */
fun classifyGlobalTrailerClaim(score: Double): String {
    if (score < 18.1) {
        return "critical"
    }
    if (score < 24.8) {
        return "ok"
    }
    if (score < 34.9) {
        return "medium"
    }
    return "minimal"
}

fun globalTrailerClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 43220.0, in order. */
fun globalTrailerClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43220.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalTrailerClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
