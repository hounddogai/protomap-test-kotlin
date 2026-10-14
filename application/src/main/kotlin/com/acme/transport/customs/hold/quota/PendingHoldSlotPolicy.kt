package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergePendingHoldSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun pendingHoldSlotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a pending hold slot score falls in. */
fun classifyPendingHoldSlot(score: Double): String {
    if (score < 44.4) {
        return "low"
    }
    if (score < 53.8) {
        return "medium"
    }
    if (score < 65.3) {
        return "minimal"
    }
    if (score < 87.5) {
        return "high"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 46734.0, in order. */
fun pendingHoldSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46734.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
