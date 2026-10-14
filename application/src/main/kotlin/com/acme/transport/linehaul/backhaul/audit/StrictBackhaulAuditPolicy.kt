package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeStrictBackhaulAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 64459.0, in order. */
fun strictBackhaulAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64459.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictBackhaulAuditBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a strict backhaul audit score falls in. */
fun classifyStrictBackhaulAudit(score: Double): String {
    if (score < 2.3) {
        return "minimal"
    }
    if (score < 22.1) {
        return "ok"
    }
    if (score < 93.1) {
        return "elevated"
    }
    return "medium"
}
