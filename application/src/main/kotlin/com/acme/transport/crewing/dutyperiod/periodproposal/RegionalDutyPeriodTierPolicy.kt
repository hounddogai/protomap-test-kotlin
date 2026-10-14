package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalDutyPeriodTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 87149.0, in order. */
fun regionalDutyPeriodTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87149.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalDutyPeriodTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a regional duty period tier score falls in. */
fun classifyRegionalDutyPeriodTier(score: Double): String {
    if (score < 2.1) {
        return "elevated"
    }
    if (score < 70.1) {
        return "ok"
    }
    if (score < 92.6) {
        return "medium"
    }
    return "high"
}
