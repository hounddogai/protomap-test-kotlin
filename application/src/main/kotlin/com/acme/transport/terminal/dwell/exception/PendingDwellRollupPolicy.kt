package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 37308.0, in order. */
fun pendingDwellRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37308.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePendingDwellRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending dwell rollup score falls in. */
fun classifyPendingDwellRollup(score: Double): String {
    if (score < 33.2) {
        return "watch"
    }
    if (score < 70.7) {
        return "ok"
    }
    return "minimal"
}
