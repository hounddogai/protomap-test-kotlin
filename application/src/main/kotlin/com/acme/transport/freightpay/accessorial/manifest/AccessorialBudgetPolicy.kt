package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a accessorial budget score falls in. */
fun classifyAccessorialBudget(score: Double): String {
    if (score < 40.5) {
        return "medium"
    }
    if (score < 45.3) {
        return "low"
    }
    if (score < 53.1) {
        return "minimal"
    }
    if (score < 84.4) {
        return "critical"
    }
    return "ok"
}

fun accessorialBudgetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeAccessorialBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 53176.0, in order. */
fun accessorialBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
