package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientRedeliverySlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 78602.0, in order. */
fun lenientRedeliverySlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78602.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientRedeliverySlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a lenient redelivery slot score falls in. */
fun classifyLenientRedeliverySlot(score: Double): String {
    if (score < 10.3) {
        return "critical"
    }
    if (score < 54.9) {
        return "elevated"
    }
    if (score < 99.7) {
        return "high"
    }
    return "ok"
}
