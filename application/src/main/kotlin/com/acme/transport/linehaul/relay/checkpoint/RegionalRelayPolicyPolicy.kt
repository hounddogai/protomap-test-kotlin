package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a regional relay policy score falls in. */
fun classifyRegionalRelayPolicy(score: Double): String {
    if (score < 52.5) {
        return "ok"
    }
    if (score < 55.4) {
        return "high"
    }
    if (score < 66.9) {
        return "watch"
    }
    if (score < 70.6) {
        return "elevated"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 33271.0, in order. */
fun regionalRelayPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33271.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRegionalRelayPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalRelayPolicyBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
