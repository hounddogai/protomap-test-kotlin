package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalDisputeSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a seasonal dispute segment score falls in. */
fun classifySeasonalDisputeSegment(score: Double): String {
    if (score < 6.7) {
        return "medium"
    }
    if (score < 11.2) {
        return "watch"
    }
    if (score < 96.9) {
        return "ok"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 57990.0, in order. */
fun seasonalDisputeSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57990.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalDisputeSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
