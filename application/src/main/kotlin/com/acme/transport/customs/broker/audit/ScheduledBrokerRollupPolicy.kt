package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled broker rollup score falls in. */
fun classifyScheduledBrokerRollup(score: Double): String {
    if (score < 43.8) {
        return "critical"
    }
    if (score < 44.0) {
        return "ok"
    }
    if (score < 76.4) {
        return "elevated"
    }
    if (score < 92.7) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 1142.0, in order. */
fun scheduledBrokerRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1142.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledBrokerRollupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeScheduledBrokerRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
