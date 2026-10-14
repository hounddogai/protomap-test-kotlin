package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun rollingDutyPeriodWindowBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 53031.0, in order. */
fun rollingDutyPeriodWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53031.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling duty period window score falls in. */
fun classifyRollingDutyPeriodWindow(score: Double): String {
    if (score < 43.7) {
        return "low"
    }
    if (score < 85.8) {
        return "minimal"
    }
    return "ok"
}

fun mergeRollingDutyPeriodWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
