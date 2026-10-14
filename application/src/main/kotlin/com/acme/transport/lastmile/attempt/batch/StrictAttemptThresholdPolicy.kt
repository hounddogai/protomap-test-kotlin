package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 74837.0, in order. */
fun strictAttemptThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74837.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictAttemptThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergeStrictAttemptThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a strict attempt threshold score falls in. */
fun classifyStrictAttemptThreshold(score: Double): String {
    if (score < 4.6) {
        return "ok"
    }
    if (score < 73.5) {
        return "high"
    }
    if (score < 73.8) {
        return "watch"
    }
    return "elevated"
}
