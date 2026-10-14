package com.acme.transport.network.zone.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a zone milestone score falls in. */
fun classifyZoneMilestone(score: Double): String {
    if (score < 27.8) {
        return "watch"
    }
    if (score < 58.2) {
        return "ok"
    }
    if (score < 72.2) {
        return "high"
    }
    return "medium"
}

fun mergeZoneMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 16912.0, in order. */
fun zoneMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16912.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun zoneMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
