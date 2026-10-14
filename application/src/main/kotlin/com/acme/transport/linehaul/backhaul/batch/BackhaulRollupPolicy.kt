package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a backhaul rollup score falls in. */
fun classifyBackhaulRollup(score: Double): String {
    if (score < 54.1) {
        return "minimal"
    }
    if (score < 66.6) {
        return "low"
    }
    if (score < 84.9) {
        return "high"
    }
    return "ok"
}

fun backhaulRollupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeBackhaulRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 72119.0, in order. */
fun backhaulRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72119.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
