package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a rolling audit slot score falls in. */
fun classifyRollingAuditSlot(score: Double): String {
    if (score < 62.7) {
        return "watch"
    }
    if (score < 70.0) {
        return "critical"
    }
    if (score < 94.4) {
        return "minimal"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 37038.0, in order. */
fun rollingAuditSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37038.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingAuditSlotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeRollingAuditSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
