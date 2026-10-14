package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 78421.0, in order. */
fun trailerProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78421.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a trailer proposal score falls in. */
fun classifyTrailerProposal(score: Double): String {
    if (score < 75.9) {
        return "low"
    }
    if (score < 82.2) {
        return "medium"
    }
    return "watch"
}

fun trailerProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun trailerProposalBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
