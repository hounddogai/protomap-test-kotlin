package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeStrictDutyPeriodEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a strict duty period estimate score falls in. */
fun classifyStrictDutyPeriodEstimate(score: Double): String {
    if (score < 2.8) {
        return "high"
    }
    if (score < 87.6) {
        return "minimal"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 59708.0, in order. */
fun strictDutyPeriodEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59708.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
