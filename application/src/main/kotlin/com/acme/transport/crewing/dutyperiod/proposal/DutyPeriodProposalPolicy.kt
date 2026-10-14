package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 55182.0, in order. */
fun dutyPeriodProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55182.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dutyPeriodProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun dutyPeriodProposalBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a duty period proposal score falls in. */
fun classifyDutyPeriodProposal(score: Double): String {
    if (score < 46.5) {
        return "low"
    }
    if (score < 84.6) {
        return "watch"
    }
    if (score < 90.4) {
        return "high"
    }
    return "minimal"
}
