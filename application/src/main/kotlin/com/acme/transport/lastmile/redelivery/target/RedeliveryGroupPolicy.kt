package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRedeliveryGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun redeliveryGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 63168.0, in order. */
fun redeliveryGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63168.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a redelivery group score falls in. */
fun classifyRedeliveryGroup(score: Double): String {
    if (score < 17.0) {
        return "minimal"
    }
    if (score < 37.9) {
        return "medium"
    }
    if (score < 96.7) {
        return "watch"
    }
    return "ok"
}
